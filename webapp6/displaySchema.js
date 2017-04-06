function getLastNode(p) { // p avec QName
	if (p==null || p=="") {
		return;
	}
	var index = p.lastIndexOf("}");
	var last = p.substring(index+1);
	if (last.indexOf("@")!=-1) {
		return last.substring(last.indexOf("@"));
	} else {
		return last;
	}
}

function removeBracket(p) {
	
	if (p==null || p=="") {
		return;
	}
	
	var s=p.split("/");
	
	for (var i=0;i<s.length;i++) {
		//console.log("s[i]",s[i]);	
		var f1 = s[i].indexOf("[");
		if (f1>=0) {			
			s[i] = s[i].substring(0,f1);
		}
	}
	return s.join("/");
}

function removeQName(p) {
	
	if (p==null || p=="") {
		return;
	}
	
	var s=p.split("/");
	
	for (var i=0;i<s.length;i++) {
		//console.log("s[i]",s[i]);
		if (s[i].indexOf("Q{")==0) {
			var i2 = s[i].indexOf("}");
			s[i] = s[i].substring(i2+1);
		}
	}
	return s.join("/");
}	

	function getSchemaRootElements(schemaDoc) {
			var result=[];
			var schemaElement = schemaDoc.firstChild;
			for(var n = 0; n < schemaElement.childNodes.length; n++) {
				if (schemaElement.childNodes[n].localName == 'element') {
					result.push(schemaElement.childNodes[n]);
				}
			}
			return result;
		}
		
		var currentSchema = null;
		
		
        function showRootElements(enableDragAndDrop,callback) {
			
			$("#schema_content").empty();
			currentSchema = null;
			
			var schema_id = $("#listSchemas").val();
			
			if (schema_id!="") {
				$.getJSON('/service/commands',{cmd:'getSchema',schema_id:schema_id}, function(schema) {	
					$("*[name=nom_schema]").text(schema.name);
					
					//showSchemaContent(schema.content);
					var doc = $.parseXML(schema.content);				
					cleanXmlSchema(doc);
					currentSchema=doc;
					$("#listRootElements").empty();
					 $("#listRootElements").append("<option></option>");
					 var rootElements = getSchemaRootElements(doc);
					 rootElements.sort(function(a, b){return a.getAttribute("name").localeCompare(b.getAttribute("name"))});
					 $.each(rootElements,function(i,s) {
						var elementName = s.getAttribute("name");
						var ns_name = get_ns_name_from_QName(schema.pref_root);
						var ns_element = get_ns_element_name(s);
						var selected="";
						if (ns_name.ns == ns_element.ns && ns_name.name == ns_element.name) {
							selected="selected";
						}
						var option = $("<option "+selected+">"+elementName+"</option>");
						option.data('schemaNode',s);
						$("#listRootElements").append(option); 
					 });
					 showSchema(enableDragAndDrop,callback);
					 
				});
			}
		}
		
		function showSchema(enableDragAndDrop,callback) {			
			var n = $("#listRootElements option:selected").data('schemaNode');
			showSchemaContent(currentSchema,n,enableDragAndDrop);
			if (callback!=null) {				
				callback( n);
			}			
		}
		
		
		/* deprecated */
		function showSchemaContent(doc,rootNode,enableDragAndDrop) {			
			$.get( 'template/schema_template.html' ).then( function ( template ) {
				
			  var currentSchemaRactive = new Ractive({
				  el: '#schema_content',
				  template: template,
				  append:false,
				  data:  {
						root: {node:rootNode.parentNode,child:[{node:rootNode,name:get_ns_element_name(rootNode),parent:null,level:0,isOpen:false,isShown:true,child:null}]}						
				  }
				});
				
				currentSchemaRactive.on({
				rightClick:function(event) {
					if (event.original.which != 3) {
						return;
					}
					rightClicOnXmlSchema(event.context);
					
				},
							  clickOnNode: function ( event ) { 
								if (event.original.which == 3) {
									rightClicOnXmlSchema(event.context);
									return ;
								}
								data = event.context;
								
								//event.context.isShown = !event.context.isShown;
								
								
								//if (1==1) return;
								
								if (data.isOpen == true) {
									data.isOpen = false;
									$.each(data.child,function(key,val) {
										val.isShown = false;
									});
									
								} else { // data.isOpen == false
									data.isOpen = true;
									if (data.child!=null) {
										$.each(data.child,function(key,val) {
											val.isShown = true;
										});
									} else {
										//constrcut child
										data.child=[];
										var elementStructure = getSchemaElementStructure(data.node);
										if (elementStructure.localName =='complexType' ) {
											// start with attributes
											$.each(elementStructure.childNodes,function(key,val) {
												if (val.localName == 'attribute') {
													
													data.child.push({node:val,name:"@"+get_ns_element_name(val),parent:data,type:val.getAttribute("type") || val.getAttribute("ref"),level:data.level+1,isOpen:false,isShown:true,child:[]});
												}
											});
											
											$.each(elementStructure.childNodes,function(key,val) {
												if (val.localName != 'attribute') { // could be sequence,choice,element or simpleContent
													processSequenceOrChoiceOrElement(val,data);
													//data.child.push({node:val,level:data.level+1,isOpen:false,isShown:true,child:[]});
												}
											});
											
										} else {
											console.log("onclick : elementStructure "+elementStructure.localName+" not implemented TODO");
										}
										
										
										
										
									}
									
								}
								currentSchemaRactive.update();
								if (enableDragAndDrop) {
									$(".bouger").draggable({appendTo: 'body',zIndex: 100,helper:"clone",opacity:0.9,cursor:"move",cursorAt:{top:-10,left:-10}}) ;
								}
								 
							  }
				});
				
				
			});
			
		}
		
		function processSequenceOrChoiceOrElement(element,node) {
			if (element.localName == 'element') {
				var elementStructure = getSchemaElementStructure(element);
				
				var d;
				//console.log("pour le node : ",element,"elementStructure=",elementStructure);
				if (typeof elementStructure === "string") {
					// type is a simple type so no children										
					type=elementStructure;					
					ns_element = get_ns_element_name(element);	
					d = {
						"iconCls":"task",
						"name":ns_element.name,
						"ns":ns_element.ns,
						"leaf":true,
						expanded: false,
						"description":"",
						type:type,
						schemaNode:element
					};	
				} else {
					
					if (elementStructure.firstChild!=null && elementStructure.firstChild.localName == 'simpleContent') {
						// complextype but of simpleContent so no children , unless attributes !! TODO
						type = elementStructure.firstChild.firstChild.getAttribute("base");
						
						
						ns_element = get_ns_element_name(element);
						d = {
							"iconCls":"task",
							"name":ns_element.name,
							"ns":ns_element.ns,
							"leaf":true,
							expanded: false,
							"description":"",
							type:type,
							schemaNode:element,
							children:[]
						};	
						
						$.each(elementStructure.firstChild.firstChild.childNodes,function(key,val) {
							if (val.localName == 'attribute') {							
								ns_element = get_ns_element_name(val);
								d.children.push({"iconCls":"task",schemaNode:val,name:"@"+ns_element.name,ns:ns_element.ns,type:val.getAttribute("type")||val.getAttribute("ref"),leaf:true});
							}
						});
						
						if (d.children.length>0) {
							d.leaf=false;
						}
					} else if (elementStructure.localName == 'simpleType') {
						// complextype but of simpleContent so no children , unless attributes !! TODO
						
						type = elementStructure.getAttribute("name");
						ns_element = get_ns_element_name(element);						
						d = {
							"iconCls":"task",
							"name":ns_element.name,
							"ns":ns_element.ns,
							"leaf":true,
							expanded: false,
							"description":"",
							type:type,
							schemaNode:element,
							children:[]
						};	
					} else {
						// complexType 
						
						type = "";
						ns_element = get_ns_element_name(element);
						
						//elementStructure.ownerDocument.firstChild.getAttribute("xmlns");
						
						if (element.getAttribute("type")!=null) {
							var t = element.getAttribute("type");
							type=t;
							if (t.indexOf(":")>=0) {
								var p = t.split(":");
								ns_element.ns = getSchemaNamespaceURIFromPrefix(element.ownerDocument,p[0]);
							}							
						}
						
						
						d = {
							"iconCls":"task",
							"name":ns_element.name,
							"ns":ns_element.ns,
							"leaf":false,
							expanded: false,
							"description":"",
							type:type,
							schemaNode:element,
							children:[]
						};	
					}
				}
				if( d!=null) {
					
					node.appendChild(d);
				}

			} else if (element.localName == 'simpleContent') {
				
				
				$.each(element.firstChild.childNodes,function(key,val) {
							if (val.localName == 'attribute') {							
								//ns_element = get_ns_element_name(val);
								//node.app.push({"iconCls":"task",schemaNode:val,name:ns_element.name,ns:ns_element.ns,type:val.getAttribute("type")||val.getAttribute("ref"),leaf:true});
								var n = {
									"iconCls":"task",
									"name":"@"+get_ns_element_name(val).name,
									// pas de ns car attribute (pourtant )
									"leaf":true,
									expanded: false,
									"description":"attribute",
									type:val.getAttribute("type") || val.getAttribute("ref"),
									schemaNode:val
								};													
								
								node.appendChild(n);
							}
						});		
					// node.children.push ou bien node.appendChild
			} else	if (element.localName == 'choice' || element.localName == 'sequence') {
				$.each(element.childNodes,function(k,val) {
					processSequenceOrChoiceOrElement(val,node);
				});
			}  else if (element.localName == 'any') {
				// <any namespace="##other" processContents="strict"/>
				
				
				d = {
							"iconCls":"task",
							"name":"ANY ELEMENT",
							"ns":"",
							"leaf":true,
							expanded: false,
							"description":"",
							type:"any",
							schemaNode:element,
							children:[]
						};	
						node.appendChild(d);
			}
			
			else {
				console.log(" processSequenceOrChoiceOrElement : "+element.localName+" not implemented : TODO");
			}
			
			
		}
		
		function recurseGetSchemaAttributeAndChild(node) {
			var childs=[];
			if (node.localName == 'element') {
				var elementStructure = getSchemaElementStructure(node);
				if (typeof elementStructure === "string") {
					// type is a simple type so no children
					
					
					type=elementStructure;
				} else if (elementStructure.localName == 'simpleType') {
					// complextype but of simpleContent so no children , unless attributes !! TODO
					type = elementStructure.getAttribute("name");
				
				} else if (elementStructure.firstChild.localName == 'simpleContent') {
					// complextype but of simpleContent so no children , unless attributes !! TODO
					type = elementStructure.firstChild.firstChild.getAttribute("base");
				
				} else if (elementStructure.firstChild.localName == 'union') {
					// complextype but of simpleContent so no children , unless attributes !! TODO
					type = elementStructure.firstChild.getAttribute("memberTypes");
				
				} else {
					// choice ou sequence
					type = elementStructure.firstChild.localName;
				}
				
				
				childs.push({node:node,type:type});
			} else if (node.localName == 'simpleContent') {
				//console.log("simpleContent TODO");
				$.each(node.firstChild.childNodes,function(key,val) {
					if (val.localName == 'attribute') {
						childs.push({node:val,type:val.getAttribute("type")||val.getAttribute("ref")});								
					}
				});
			} else	if (node.localName == 'choice' || node.localName == 'sequence') {
				$.each(node.childNodes,function(k,subnode) {
					var c = recurseGetSchemaAttributeAndChild(subnode);
					childs = childs.concat(c);
				});
			} else {
				console.log(" processSequenceOrChoiceOrElement : "+node.localName+" not implemented : TODO");
			}
			
			return childs;
		}
		
		
		function getRootNode(rootElements,element_name) {
			for (var i=0;i<rootElements.length;i++) {
				var r = rootElements[i];
				if (r.getAttribute("name") == element_name || r.getAttribute("ref") == element_name) {
					return r;
				}
			}
			
			return null;
		}
		
		function getSchemaAttributeAndChild(node) {
			var childs = [];
			var elementStructure = getSchemaElementStructure(node);
			if (elementStructure.localName =='complexType') {
				// start with attributes
				$.each(elementStructure.childNodes,function(key,val) {
					if (val.localName == 'attribute') {						
						childs.push({node:val,type:val.getAttribute("type")});
					}
				});
				
				$.each(elementStructure.childNodes,function(key,val) {
					if (val.localName != 'attribute') { // could be sequence,choice,element or simpleContent
						var c = recurseGetSchemaAttributeAndChild(val);		
						childs = childs.concat(c);					
					}
				});
			} else {
				//console.log("TODO : getSchemaAttributeAndChild not complexType, is ",elementStructure.localName);
			}
			return childs;
		}
		
		var foreignSchema={}; // "uri"->schema
		
		function getSchemaNamespaceURIFromPrefix(schemaDoc,prefix) {
		/*
			<xs:schema elementFormDefault="qualified" xmlns:prefix0="http://www.openarchives.org/OAI/2.0/" xmlns:xs="http://www.w3.org/2001/XMLSchema">
		  <xs:import namespace="http://www.openarchives.org/OAI/2.0/" schemaLocation="http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd"/>
		  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="http://www.w3.org/2001/03/xml.xsd"/>
		*/
			var ns = schemaDoc.firstChild.getAttribute("xmlns:"+prefix);
			
			return ns;
		}
		
		function getSchemaDocFor(schemaDoc,namespace_uri) {
			var foreignSchemaDoc = foreignSchema[namespace_uri];
			if (foreignSchemaDoc==null) {
				//console.log("schema with uri :"+namespace_uri+" not found, trying to know the xsd");
				// on determine l'url du schema (via les xs:import)
				var schemaElement = schemaDoc.firstChild;
				for(var n = 0; n < schemaElement.childNodes.length; n++) {
					var sc = schemaElement.childNodes[n];
					if (sc.localName == 'import' && sc.getAttribute("namespace")==namespace_uri) {
						//System.out.println("got an import",schemaElement);
						var schemaLocation = sc.getAttribute("schemaLocation");
						
						
						// load the schemaLocation
						//console.log("loading of : schema : ",schemaLocation);
						$.ajax({
						  url: "/service/getSchema",						  
						  data: { url: schemaLocation},
						  async:false
						}).done(function(data) {
							var doc = $.parseXML(data);				
							cleanXmlSchema(doc);  							
							foreignSchemaDoc = doc
							foreignSchema[namespace_uri] = foreignSchemaDoc;
						});
						
						
					} 
				}
			}
			return foreignSchemaDoc;
		}
		
		function getSchemaType(schemaDoc,typeName) {
			//console.log("getSchemaType",schemaDoc,typeName);
			var defaultNamespace = schemaDoc.firstChild.getAttribute("xmlns");
			
			
			if (typeName.indexOf("xs:") == 0) {
				return typeName;
			}
			
			if (typeName.indexOf("xml:") == 0) {
				return typeName;
			}
			
			if (typeName.indexOf("t:")==0) {
				typeName = typeName.substr(2);
			}
			
			if (typeName.indexOf(":")>=0) {
				var targetNamespace = schemaDoc.firstChild.getAttribute("targetNamespace");
				var n = typeName.split(":");
				var prefix = n[0];
				var type = n[1];
				
				var ns = getSchemaNamespaceURIFromPrefix(schemaDoc,prefix);
				if (ns == targetNamespace) {
					// nous avons un type du genre : XX:YY et XX est en fait le target namespace
					// donc cela signifie que l'on recherche un type du schema Actuel
					typeName = type;
				}
				
			}
			
			//console.log("find of type with name ",typeName);
			var schemaElement = schemaDoc.firstChild;
			for(var n = 0; n < schemaElement.childNodes.length; n++) {
				if (schemaElement.childNodes[n].localName == 'complexType' && schemaElement.childNodes[n].getAttribute("name") == typeName ) {
					return schemaElement.childNodes[n];
				} else if (schemaElement.childNodes[n].localName == 'simpleType' && schemaElement.childNodes[n].getAttribute("name") == typeName ) {
					return schemaElement.childNodes[n];
				} else if (schemaElement.childNodes[n].localName == 'element' && schemaElement.childNodes[n].getAttribute("name") == typeName ) {
					//return schemaElement.childNodes[n];
					return getSchemaElementStructure(schemaElement.childNodes[n]);
				} 
			}
			
			if (defaultNamespace=='http://www.w3.org/2001/XMLSchema' && typeName.indexOf(":")<0) {
				return typeName; // xml schema type name
			}
			
			if (typeName.indexOf(":")>=0) {				// foreign type
				var n = typeName.split(":");				
				//console.log("prefix = ",n[0]);
				//console.log("typename = ",n[1]);
				var ns = getSchemaNamespaceURIFromPrefix(schemaDoc,n[0]);
				//console.log("ns of the prefix",ns);
				var foreignSchemaDoc = getSchemaDocFor(schemaDoc,ns);
				return getSchemaType(foreignSchemaDoc,n[1]);
				
			}
			
			console.log("getSchemaType : type not found ",typeName);
			
			return null;
		}
		
		function getAllSchemaType() {
			var types = new Array();
			var schemaElement = currentSchema.firstChild;
			for(var n = 0; n < schemaElement.childNodes.length; n++) {
				if (schemaElement.childNodes[n].localName == 'complexType' ) {
					types.push(schemaElement.childNodes[n].getAttribute("name"));
				} else if (schemaElement.childNodes[n].localName == 'simpleType'  ) {
					types.push(schemaElement.childNodes[n].getAttribute("name"));
				} 
			}
			return types;
		}
		
		function getRefElement(schemaDoc,ns,refName) {
		var schemaElement = schemaDoc.firstChild;
			for(var n = 0; n < schemaElement.childNodes.length; n++) {
				//TODO : must search also with the namespace ! (using targetNamespace)
				if (schemaElement.childNodes[n].localName == 'element' && schemaElement.childNodes[n].getAttribute("name") == refName) {
					return schemaElement.childNodes[n];
				}
			}
			console.log("getRefElement : ref not found ",refName);
			return null;
		}

		function get_ns_name_from_QName(str) {
			if (str == null) {
				return {ns:"",name:""};
			}
			var name = str;
			var ns="";
			if (name.indexOf("Q{")==0) {
				// element with namespace
				var x = name.split("}");					
				ns = x[0].substring(2);
				name = x[1];
			}
			return {ns:ns,name:name};
		}
		
		function get_ns_element_name(element) { // attr : 'ref' ou 'name'
			if (element.getAttribute('ref')!=null) {
				return  {ns:"",name:element.getAttribute('ref')};
			} else {
				var name = element.getAttribute('name');
				var ns="";
				if (name.indexOf(":")>=0) {
					// element with namespace
					var x = name.split(":");					
					ns = element.getAttribute("xmlns:"+x[0]);
					name = x[1];
				}
				
				if (ns=="" && element.parentNode.localName=="schema") {
					var targetNamespace = element.parentNode.getAttribute("targetNamespace");
					ns = targetNamespace;
					
				}
				var schemaDoc = element.ownerDocument;
				var schemaElement = schemaDoc.firstChild;
				var targetNamespace = schemaElement.getAttribute("targetNamespace");
				
				if (ns == "") {
						ns=targetNamespace; // laurent 
					}
				if (ns==null)	{
					ns="";
				}
				return {ns:ns,name:name};
			}
		}
		
		function schemaChangeType(element,newTypeName) {
			//console.log("schemaChangeType : ",element);
			// simple element with type attribute :
			if (element.hasAttribute("type")) {
				console.log("has type");
				element.setAttribute("type",newTypeName);
				return;
			} 
			if (element.hasAttribute("ref")) {
				console.log("has ref");
				var schemaDoc = element.ownerDocument;
				var n = get_ns_element_name(element);
				var el = getRefElement(schemaDoc,n.ns,n.name);
				console.log("ref element = ",el);
				schemaChangeType(el,newTypeName);
				return;
			}
			
			// self content type : complexType or simpleContent
			console.log("schemaChangeType : complextype TODO");
			// TODO remplacer plusieurs noeud par le noeud "type", todo : il faudrait que le param newTypeName soit en fait un noeud du schema 
			var fc = element.firstChild;
			
			
			
			
			if (fc.localName == "complexType") {
				var fc2 = fc.firstChild;
				if (fc2.localName == "simpleContent") {
					var fc3 = fc2.firstChild;
					if (fc3.localName == "extension") {
						fc3.setAttribute("base",newTypeName);
						return;
					}
				}
			}
			// si le message s'affiche c'est que le typage ne s'est pas effectué
			console.log(new XMLSerializer().serializeToString(fc));
			
			return;
			
		}
		
		function getSchemaElementStructure(element) {
	
			var schemaDoc = element.ownerDocument;
			
			if (element.hasAttribute("type")) {
				//console.log("getSchemaElementStructure : element ",element.getAttribute("name"),"has type",element.getAttribute("type"));
				return getSchemaType(schemaDoc,element.getAttribute("type"));
			} else if (element.hasAttribute("ref")) {
				//console.log("getSchemaElementStructure : element has ref",element.getAttribute("ref"));
				var n = get_ns_element_name(element);
				var el = getRefElement(schemaDoc,n.ns,n.name);
				return getSchemaElementStructure(el); // on reitere car l'element de reference peut être composé d'un type			
			} else {
				//console.log("getSchemaElementStructure : element is self contened");
				return element.firstChild;
			}
		}

		function getPathFromData(data,currentPath) {
			if (data==null) {
				return currentPath;
			}
			
			currentPath.push(data.data.schemaNode);
			
			return getPathFromData(data.parentNode,currentPath);
			
		}

		function getFullPathInfo(data) {
			// return object with : pathAsArray, fullPathAsString, type
			var obj={pathAsNode:[],pathAsArray:[],pathAsString:'',pathWithNSAsString:'',type:null};
			obj.pathAsNode = getPathFromData(data,[]).reverse();
			
			
			for (var i=0;i<obj.pathAsNode.length;i++) {
				var element = obj.pathAsNode[i];
				//console.log(element);
				//console.log(element.parentNode);
				var elementName = get_ns_element_name(element);
				obj.pathAsArray.push(elementName.name);
				
				if (element.localName =='element') {
					obj.pathAsString+='/'+elementName.name;
					obj.pathWithNSAsString+='/Q{'+elementName.ns+"}"+elementName.name;
				} else if (element.localName=='attribute') {
					obj.pathAsString+='/@'+elementName.name;
					
					//obj.pathWithNSAsString+='/@Q{'+elementName.ns+"}"+elementName.name;
						obj.pathWithNSAsString+='/@'+elementName.name;
					
					
				}
			}
			
			
			obj.type = data.type;
			
			return obj;
		}

		
		
		
			
		var reBlank = /^\s*$/;
		function cleanXmlSchema(node) {
			var child, next;
			switch (node.nodeType) {
				case 3: // Text node
					if (reBlank.test(node.nodeValue)) {
						node.parentNode.removeChild(node);
					}
					break;
				case 8: // comment
					node.parentNode.removeChild(node);
					break;
				case 1: 
					if (node.localName=='annotation') {
							node.parentNode.removeChild(node);
							break;
						}
				case 9: // Document node
					
					child = node.firstChild;
					while (child) {
						next = child.nextSibling;
						cleanXmlSchema(child);
						child = next;
					}
					break;
			}
		};
		
		
function cleanXml(node) {
    var child, next;
    switch (node.nodeType) {
        case 3: // Text node
            if (reBlank.test(node.nodeValue)) {
                node.parentNode.removeChild(node);
            }
            break;
		/*
		case 7:
			node.parentNode.removeChild(node);
			break;
		*/
        case 1: // Element node
        case 9: // Document node
            child = node.firstChild;
            while (child) {
                next = child.nextSibling;
                cleanXml(child);
                child = next;
            }
            break;
    }
};

function isNumericSchemaType(type) {
	if (type == "integer_emptyString") {
		return true;
	}
	
	if (type == "xs:integer") {
		return true;
	}
}
function xmlToString(xmlData) { 

    var xmlString;
    //IE
    if (window.ActiveXObject){
        xmlString = xmlData.xml;
    }
    // code for Mozilla, Firefox, Opera, etc.
    else{
        xmlString = (new XMLSerializer()).serializeToString(xmlData);
    }
    return xmlString;
}   


function getSchemaNodeFromPath(path) { // ex : /Q{}corpus/Q{}texte/Q{}auteur
	var p = removeQName(path).split("/");
	//console.log("p=",p);
	if (p[0]=="") {
		p.shift();
	}
	
	var schemaWin = Ext.getCmp("theschema");
	var store = schemaWin.down("treepanel").getStore();
	
	
	//console.log("schemaWin=",store); // Ext.data.TreeStore
	//console.log("root=",store.getRoot()) // data.name = "corpus", puis childNodes (si size = 0) alors faire un expand() si not leaf jusqu'a avoir l'élément voulu
	
	
	var currentNode = store.getRoot();
	if (currentNode.data.name != p[0]) {
		return null;
	}
	
	for (var i=1;i<p.length;i++) {
		// search in childnode
		if (!currentNode.hasChildNodes( )) {
			currentNode.expand();
		}
		var foundChild = null;
		for (var j=0;j<currentNode.childNodes.length;j++) {
			//console.log(currentNode.childNodes[j].data);
			if (p[i][0]=="@") {
				if (currentNode.childNodes[j].data.schemaNode.localName  == "attribute" && (currentNode.childNodes[j].data.name) == p[i]) { // "@"+currentNode.childNodes[j].data.name
					foundChild = currentNode.childNodes[j];
					break;
				}
			} else {
				if (currentNode.childNodes[j].data.name == p[i]) {
					foundChild = currentNode.childNodes[j];
					break;
				}
			}
		}
		
		if (foundChild!=null) {
			currentNode = foundChild;
		} else {
			return null; // one child not found
		}
		
	}
	
	return currentNode;
	//console.log("currentSchema=",currentSchema);
	/*
	var currentSchemaNode = null;
	var rootElements = getSchemaRootElements(currentSchema);
	for (var i=0;i<rootElements.length;i++) {
		var el = rootElements[i];
		var ns_element = get_ns_element_name(el); // {ns:ns,name:name}
		
		if (ns_element.name == p[0]) {
			currentSchemaNode = {
					"iconCls":"task-folder", // task-folder
					"name":ns_element.name,
					"ns":ns_element.ns,
					"leaf":false,
					expanded: false,
					"description":"",
					schemaNode:el,
					children:[],
					allowDrag:true
				};
			break;
		}
	}
	*/
	
	
}


