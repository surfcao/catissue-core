
package krishagni.catissueplus.bizlogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import krishagni.catissueplus.dao.SpecimenDAO;
import krishagni.catissueplus.dto.AliquotContainerDetailsDTO;
import krishagni.catissueplus.dto.AliquotDetailsDTO;
import krishagni.catissueplus.dto.ContainerInputDetailsDTO;
import krishagni.catissueplus.dto.ExternalIdentifierDTO;
import krishagni.catissueplus.dto.SingleAliquotDetailsDTO;
import krishagni.catissueplus.dto.SpecimenDTO;
import krishagni.catissueplus.Exception.CatissueException;
import krishagni.catissueplus.Exception.SpecimenErrorCodeEnum;
import edu.wustl.catissuecore.domain.ExternalIdentifier;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCharacteristics;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.SpecimenPosition;
import edu.wustl.catissuecore.printserviceclient.LabelPrinter;
import edu.wustl.catissuecore.printserviceclient.LabelPrinterFactory;
import edu.wustl.catissuecore.util.PrintUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.Variables;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.exception.ApplicationException;
import edu.wustl.common.exception.BizLogicException;
import edu.wustl.common.util.global.Status;
import edu.wustl.dao.HibernateDAO;
import edu.wustl.dao.exception.DAOException;
import edu.wustl.dao.query.generator.DBTypes;
import edu.wustl.dao.util.NamedQueryParam;
import edu.wustl.security.manager.SecurityManagerFactory;

public class AliquotBizLogic
{

    private SpecimenDTO createAliquotSpecimenDTOFromDTO(AliquotDetailsDTO aliquotDetailsDTO, Specimen parentSpecimen,
            SingleAliquotDetailsDTO singleAliquotDetailsDTO, SpecimenPosition spePositionObj)
    {
        SpecimenDTO specimenDto = new SpecimenDTO();
        specimenDto.setActivityStatus(Status.ACTIVITY_STATUS_ACTIVE.toString());
        specimenDto.setAvailable(Boolean.TRUE);
        specimenDto.setAvailableQuantity(singleAliquotDetailsDTO.getQuantity());

        specimenDto.setClassName(parentSpecimen.getSpecimenClass());
        specimenDto.setCollectionStatus(Constants.COLLECTION_STATUS_COLLECTED);
        specimenDto.setComments("");
        specimenDto.setConcentration((parentSpecimen).getConcentrationInMicrogramPerMicroliter());
        if (spePositionObj != null)
        {
            specimenDto.setContainerId(spePositionObj.getStorageContainer().getId());
            specimenDto.setPos1(spePositionObj.getPositionDimensionOneString());
            specimenDto.setPos2(spePositionObj.getPositionDimensionTwoString());
            specimenDto.setContainerName(singleAliquotDetailsDTO.getStoragecontainer());
        }
        specimenDto.setCreatedDate(aliquotDetailsDTO.getCreationDate());
        specimenDto.setLineage(Constants.ALIQUOT);
        specimenDto.setParentSpecimenId(parentSpecimen.getId());
        specimenDto.setParentSpecimenName(parentSpecimen.getLabel());
        specimenDto.setPathologicalStatus(parentSpecimen.getPathologicalStatus());
        specimenDto.setQuantity(singleAliquotDetailsDTO.getQuantity());
        specimenDto.setSpecimenCollectionGroupId(parentSpecimen.getSpecimenCollectionGroup().getId());
        specimenDto.setTissueSide(parentSpecimen.getSpecimenCharacteristics().getTissueSide());
        specimenDto.setTissueSite(parentSpecimen.getSpecimenCharacteristics().getTissueSite());
        specimenDto.setExternalIdentifiers(new ArrayList<ExternalIdentifierDTO>());
        specimenDto.setType(parentSpecimen.getSpecimenType());
        if (!Variables.isTemplateBasedLblGeneratorAvl && !Variables.isSpecimenLabelGeneratorAvl)
        {
            specimenDto.setLabel(singleAliquotDetailsDTO.getAliqoutLabel());
        }
        if (!Variables.isSpecimenBarcodeGeneratorAvl && !Variables.isTemplateBasedLblGeneratorAvl)
        {
            specimenDto.setBarcode(singleAliquotDetailsDTO.getBarCode());
        }
        return specimenDto;
    }

    /**
     * Validate availableQuantity against quantity per aliquot 
     * @param aliquotDetailObj
     * @param availableQuantity
     * @throws ApplicationException
     */
    private double getTotalDistributedQuantity(Collection<SingleAliquotDetailsDTO> aliquotDetailsColl,
            double availableQuantity) throws ApplicationException
    {
        double totalDistributedQuantity = 0;
        Iterator<SingleAliquotDetailsDTO> aliquotDetailsIterator = aliquotDetailsColl.iterator();
        while (aliquotDetailsIterator.hasNext())
        {
            SingleAliquotDetailsDTO aliquotDetailsDTO = aliquotDetailsIterator.next();
            totalDistributedQuantity = totalDistributedQuantity + aliquotDetailsDTO.getQuantity();
        }
        if (totalDistributedQuantity > availableQuantity)
        {
            throw new CatissueException(SpecimenErrorCodeEnum.INSUFFICIENT_AVAILABLE_QUANTITY.getDescription(),
                    SpecimenErrorCodeEnum.INSUFFICIENT_AVAILABLE_QUANTITY.getCode());

        }
        return totalDistributedQuantity;
    }

    /**
     * This api create aliquots
     * @param aliquotDetailObj
     * @param sessionDataBean
     * @throws CloneNotSupportedException 
     * @throws Exception 
     */
    public void createAliquotSpecimen(AliquotDetailsDTO aliquotDetailsDTO, HibernateDAO hibernateDao,
            SessionDataBean sessionDataBean) throws ApplicationException, CloneNotSupportedException
    {

        SpecimenDAO specimenDAO = new SpecimenDAO();
        Specimen parentSpecimen = specimenDAO.getParentSpecimen(hibernateDao, aliquotDetailsDTO.getParentLabel());
        aliquotDetailsDTO.setParentId(parentSpecimen.getId());
        List<SingleAliquotDetailsDTO> aliquotDetailList = aliquotDetailsDTO.getPerAliquotDetailsCollection();
        StorageContainerBizlogic storageContainerBizlogic = new StorageContainerBizlogic();
        double totalDistributedQuantity = getTotalDistributedQuantity(aliquotDetailList,
                parentSpecimen.getAvailableQuantity());
        SpecimenBizLogic specimenBizLogic = new SpecimenBizLogic();
        List<SpecimenDTO> specimenDtoList = new ArrayList<SpecimenDTO>();
        parentSpecimen = specimenBizLogic.getParent(parentSpecimen.getLabel(), hibernateDao);
        for (int cnt = 0; cnt < aliquotDetailList.size(); cnt++)
        {
            SingleAliquotDetailsDTO singleAliquotDetailsDTO = aliquotDetailList.get(cnt);
            SpecimenPosition specimenPosition = null;
            if (!singleAliquotDetailsDTO.getStoragecontainer().equals(Constants.STORAGE_TYPE_POSITION_VIRTUAL))
            {
                specimenPosition = storageContainerBizlogic.getPositionIfAvailableFromContainer(
                        singleAliquotDetailsDTO.getStoragecontainer(), singleAliquotDetailsDTO.getPos1(),
                        singleAliquotDetailsDTO.getPos2(), hibernateDao);
                singleAliquotDetailsDTO.setPos1(specimenPosition.getPositionDimensionOneString());
                singleAliquotDetailsDTO.setPos2(specimenPosition.getPositionDimensionTwoString());
            }

            SpecimenDTO aliquotSpecimen = createAliquotSpecimenDTOFromDTO(aliquotDetailsDTO, parentSpecimen,
                    singleAliquotDetailsDTO, specimenPosition);

            Specimen specimen = specimenBizLogic.getSpecimen(aliquotSpecimen, hibernateDao, sessionDataBean,
                    parentSpecimen);
            specimenBizLogic.insertSpecimen(hibernateDao, specimen);

            parentSpecimen.getChildSpecimenCollection().add(specimen);
            specimenDtoList.add(specimenBizLogic.getSpecimenDTO(specimen));

        }
        //  specimenDtoList = specimenBizLogic.insert(specimenDtoList, hibernateDao, sessionDataBean);
        for (int i = 0; i < specimenDtoList.size(); i++)
        {

            aliquotDetailList.get(i).setAliqoutLabel(specimenDtoList.get(i).getLabel());
            aliquotDetailList.get(i).setBarCode(specimenDtoList.get(i).getBarcode());
            aliquotDetailList.get(i).setAliquotId(specimenDtoList.get(i).getId());
        }

        if (aliquotDetailsDTO.isDisposeParentSpecimen())
        {

            specimenBizLogic.disposeSpecimen(hibernateDao, sessionDataBean, parentSpecimen,
                    Constants.SPECIMEN_DISPOSAL_REASON);
        }
        specimenBizLogic.reduceQuantity(totalDistributedQuantity, parentSpecimen.getId(), hibernateDao);
        aliquotDetailsDTO.setCurrentAvailableQuantity(parentSpecimen.getAvailableQuantity());

    }

    public AliquotDetailsDTO getAliquotDetailsDTO(String parentSpecimentLabel, int aliquotCount,
            Double quantityPerAliquot, HibernateDAO hibernateDao, SessionDataBean sessionDataBean)
            throws ApplicationException
    {
        AliquotDetailsDTO aliquotDetailsDTO = new AliquotDetailsDTO();
        SpecimenDAO specimenDAO = new SpecimenDAO();
        Specimen parentSpecimen = specimenDAO.getParentSpecimen(hibernateDao, parentSpecimentLabel);
        if (quantityPerAliquot != null && ((quantityPerAliquot * aliquotCount) > parentSpecimen.getAvailableQuantity()))
        {
            throw new CatissueException(SpecimenErrorCodeEnum.INSUFFICIENT_AVAILABLE_QUANTITY.getDescription(),
                    SpecimenErrorCodeEnum.INSUFFICIENT_AVAILABLE_QUANTITY.getCode());

            //			throw new ApplicationException(null, null, Constants.INSUFFICIENT_AVAILABLE_QUANTITY);
        }
        aliquotDetailsDTO.setConcentration(parentSpecimen.getConcentrationInMicrogramPerMicroliter());
        aliquotDetailsDTO.setCreationDate(new Date());

        aliquotDetailsDTO.setCurrentAvailableQuantity(parentSpecimen.getAvailableQuantity());
        aliquotDetailsDTO.setInitialAvailableQuantity(parentSpecimen.getInitialQuantity());
        aliquotDetailsDTO.setParentId(parentSpecimen.getId());
        aliquotDetailsDTO.setParentLabel(parentSpecimen.getLabel());
        aliquotDetailsDTO.setPathologicalStatus(parentSpecimen.getPathologicalStatus());
        aliquotDetailsDTO.setSpecimenClass(parentSpecimen.getSpecimenClass());
        aliquotDetailsDTO.setType(parentSpecimen.getSpecimenType());
        aliquotDetailsDTO.setTissueSide(parentSpecimen.getSpecimenCharacteristics().getTissueSide());
        aliquotDetailsDTO.setTissueSite(parentSpecimen.getSpecimenCharacteristics().getTissueSite());
        aliquotDetailsDTO.setConcentration(parentSpecimen.getConcentrationInMicrogramPerMicroliter());
        List<SingleAliquotDetailsDTO> singleAliquotDetailsDTOList = new ArrayList<SingleAliquotDetailsDTO>();

        if (quantityPerAliquot == null)
        {
            quantityPerAliquot = parentSpecimen.getAvailableQuantity() / aliquotCount;
        }
        Long cpId = specimenDAO.getCpIdFromSpecimenLabel(aliquotDetailsDTO.getParentLabel(), hibernateDao);
        ContainerInputDetailsDTO containerInputDetail = new ContainerInputDetailsDTO();
        containerInputDetail.aliquotCount = aliquotCount;
        containerInputDetail.cpId = cpId;
        containerInputDetail.isAdmin = sessionDataBean.isAdmin();
        containerInputDetail.userId = sessionDataBean.getUserId();
        containerInputDetail.specimenClass = aliquotDetailsDTO.getSpecimenClass();
        containerInputDetail.specimenType = aliquotDetailsDTO.getType();
        AliquotContainerDetailsDTO aliquotContainerDetails = getAliquotContainerDetails(containerInputDetail,
                hibernateDao);
        for (int i = 0; i < aliquotCount; i++)
        {
            SingleAliquotDetailsDTO singleAliquotDetailsDTO = new SingleAliquotDetailsDTO();
            singleAliquotDetailsDTO.setQuantity(quantityPerAliquot);
            singleAliquotDetailsDTO.setAliqoutLabel("");
            singleAliquotDetailsDTO.setBarCode("");
            singleAliquotDetailsDTO.setStoragecontainer(aliquotContainerDetails.containerName);
            /*
             * Added below if condition because there is one legacy bug in getAliquotContainerDetails api
             * while returning empty position it ignores the occupied child containers
             * */
            if(aliquotContainerDetails.position1.size()>i){
                singleAliquotDetailsDTO.setPos1(aliquotContainerDetails.position1.get(i));
                singleAliquotDetailsDTO.setPos2(aliquotContainerDetails.position2.get(i));
            }else{
                singleAliquotDetailsDTO.setPos1("");
                singleAliquotDetailsDTO.setPos2("");
               
            }
            singleAliquotDetailsDTOList.add(singleAliquotDetailsDTO);

        }

        aliquotDetailsDTO.setPerAliquotDetailsCollection(singleAliquotDetailsDTOList);
        return aliquotDetailsDTO;
    }

    public AliquotContainerDetailsDTO getAliquotContainerDetails(ContainerInputDetailsDTO containerInputDetails,
            HibernateDAO hibernateDao) throws ApplicationException
    {

        StorageContainerBizlogic storageContainerBizlogic = new StorageContainerBizlogic();

        List<AliquotContainerDetailsDTO> aliquotContainerDetailsDTOList = storageContainerBizlogic
                .getStorageContainerList(containerInputDetails, null, hibernateDao, 1);

        if (aliquotContainerDetailsDTOList == null || aliquotContainerDetailsDTOList.isEmpty())
        {
            throw new CatissueException(SpecimenErrorCodeEnum.INSUFFICIEN_STORAGE_LOCATION.getDescription(),
                    SpecimenErrorCodeEnum.INSUFFICIEN_STORAGE_LOCATION.getCode());
        }
        AliquotContainerDetailsDTO aliquotContainerDetailsDTO = aliquotContainerDetailsDTOList.get(0);
        storageContainerBizlogic.setAvailablePositionsForContainer(aliquotContainerDetailsDTO, "", "",
                containerInputDetails.aliquotCount, hibernateDao);

        return aliquotContainerDetailsDTO;
    }

    public List<SpecimenDTO> getAvailabelSpecimenList(String specimenLable, HibernateDAO hibernateDao)
            throws ApplicationException
    {
        List<SpecimenDTO> specimenDaoList = new ArrayList<SpecimenDTO>();
        Map<String, NamedQueryParam> params = new HashMap<String, NamedQueryParam>();
        params.put("0", new NamedQueryParam(DBTypes.STRING, specimenLable));
        params.put("1", new NamedQueryParam(DBTypes.STRING, specimenLable));

        List specimenDetailColl = hibernateDao.executeNamedQuery("getAvailabelSpecimenLabel", params);
        for (int i = 0; i < specimenDetailColl.size(); i++)
        {
            Object[] objectArr = (Object[]) specimenDetailColl.get(i);
            SpecimenDTO specimenDTO = new SpecimenDTO();
            specimenDTO.setLabel(objectArr[0] != null ? objectArr[0].toString() : "");
            specimenDTO.setBarcode(objectArr[1] != null ? objectArr[1].toString() : "");
            specimenDaoList.add(specimenDTO);
        }

        return specimenDaoList;
    }

}