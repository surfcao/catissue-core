<%@ page import="edu.wustl.catissuecore.actionForm.CategorySearchForm"%>
<%@ page import="java.util.*"%>
<%@ page import="java.lang.*"%>

<%@ page import="edu.wustl.catissuecore.util.global.Constants"%>
<%@ page import="edu.wustl.catissuecore.util.global.Utility"%>
<%@ page import="edu.wustl.catissuecore.util.global.Variables"%>
<%@ page import="edu.wustl.common.tree.QueryTreeNodeData"%>
<html>
<head>
	<link rel="stylesheet" type="text/css" href="css/styleSheet.css" />
	<title>DHTML Tree samples. dhtmlXTree - Action handlers</title>
	<link rel="STYLESHEET" type="text/css" href="dhtml_comp/css/dhtmlXTree.css">
	<script language="JavaScript" type="text/javascript" src="dhtml_comp/js/dhtmXTreeCommon.js"></script>
	<script language="JavaScript" type="text/javascript" src="dhtml_comp/js/dhtmlXTree.js"></script>
	<script language="JavaScript" type="text/javascript" src="jss/javaScript.js"></script>
	<link rel="STYLESHEET" type="text/css" href="dhtml_comp/css/dhtmlXGrid.css"/>
	<link rel="STYLESHEET" type="text/css" href="dhtml_comp/css/dhtmlXTree.css">
	<script  src="dhtml_comp/jss/dhtmlXCommon.js"></script>
    <script type="text/javascript" src="jss/ajax.js"></script> 
<script src="jss/queryModule.js"></script>
</head>
<%
Long trees = (Long)request.getSession().getAttribute("noOfTrees");
int noOfTrees = trees.intValue();

%>
 <script>

var trees = new Array();
function initTreeView()
{

var treeNo = 0;
		<%  
			String rootNodeIdOfFirstTree = "";
			boolean isrootNodeIdOfFirstTree = false;
		for(int i=0;i<noOfTrees;i++) 
		{
			String divId = "treebox"+i;
			String treeDataId = Constants.TREE_DATA+"_"+i;
			%>
			trees[treeNo]=new dhtmlXTreeObject(<%=divId%>,"100%","100%",0);
			trees[treeNo].setImagePath("dhtml_comp/imgs/");
			trees[treeNo].setOnClickHandler(treeNodeClicked);
			<%
					Vector treeData = (Vector)request.getAttribute(treeDataId);
					if(treeData != null && treeData.size() != 0)
						{
							Iterator itr  = treeData.iterator();
							String nodeColapseCode = "";
							while(itr.hasNext())
							{
								QueryTreeNodeData data = (QueryTreeNodeData) itr.next();
								String parentId = "0";	
								if(!data.getParentIdentifier().equals("0"))
								{
									parentId = data.getParentIdentifier().toString();		
								}
								String nodeId = data.getIdentifier().toString();
								if(!isrootNodeIdOfFirstTree)
								{
									rootNodeIdOfFirstTree = nodeId;
									isrootNodeIdOfFirstTree = true;
								}
								String img = "results.gif";
								if(nodeId.endsWith(Constants.LABEL_TREE_NODE))
								{
									 img = "folder.gif";
								}
								if (parentId.equals("0"))
								{
									nodeColapseCode += "tree.closeAllItems('" + nodeId + "');";
								}
			%>
			trees[treeNo].insertNewChild("<%=parentId%>","<%=nodeId%>","<%=data.getDisplayName()%>",0,"<%=img%>","<%=img%>","<%=img%>","");
			trees[treeNo].setUserData("<%=nodeId%>","<%=nodeId%>","<%=data%>");	
			<%	
							}
			}	%>
treeNo = treeNo + 1;						
		<%}
	%>	
		trees[0].selectItem("<%=rootNodeIdOfFirstTree%>",true);
}
</script>
<%
           boolean mac = false;
	        Object os = request.getHeader("user-agent");
			if(os!=null && os.toString().toLowerCase().indexOf("mac")!=-1)
			{
			    mac = true;
			}
	String height = "100%";		
	if(mac)
	{
	  height="300";
    }
%>
<body onload="initTreeView()">
<html:errors />
<%
	String formAction = Constants.DefineSearchResultsViewJSPAction;
%>
<html:form method="GET" action="<%=formAction%>">
<html:hidden property="currentPage" value=""/>
<html:hidden property="stringToCreateQueryObject" value="" />

<table border="0" width="100%" cellspacing="0" cellpadding="0" bgcolor="#FFFFFF" height="100%" bordercolorlight="#000000" class='tbBordersAllbordersBlack'>
	<tr>
		<td valign="top" width="90%" height="100%">
			<%  for(int i=0;i<noOfTrees;i++) {
			String divId = "treebox"+i;
			%>
				<div id="<%=divId%>"  style="background-color:white;overflow:auto;height:100%">
				</div>
			<% } %>
		</td>
	</tr>									
</table>
		
</html:form>
</body>
</html> 