document.oncontextmenu = function() {return false;};
var searchboxCss = "\
	.ace_search {\
	background-color: #ddd;\
	border: 1px solid #cbcbcb;\
	border-top: 0 none;\
	max-width: 325px;\
	overflow: hidden;\
	margin: 0;\
	padding: 4px;\
	padding-right: 6px;\
	padding-bottom: 0;\
	position: absolute;\
	top: 0px;\
	z-index: 99;\
	white-space: normal;\
	}\
	.ace_search.left {\
	border-left: 0 none;\
	border-radius: 0px 0px 5px 0px;\
	left: 0;\
	}\
	.ace_search.right {\
	border-radius: 0px 0px 0px 5px;\
	border-right: 0 none;\
	right: 0;\
	}\
	.ace_search_form, .ace_replace_form {\
	border-radius: 3px;\
	border: 1px solid #cbcbcb;\
	float: left;\
	margin-bottom: 4px;\
	overflow: hidden;\
	}\
	.ace_search_form.ace_nomatch {\
	outline: 1px solid red;\
	}\
	.ace_search_field {\
	background-color: white;\
	border-right: 1px solid #cbcbcb;\
	border: 0 none;\
	-webkit-box-sizing: border-box;\
	-moz-box-sizing: border-box;\
	box-sizing: border-box;\
	float: left;\
	height: 22px;\
	outline: 0;\
	padding: 0 7px;\
	width: 214px;\
	margin: 0;\
	}\
	.ace_searchbtn,\
	.ace_replacebtn {\
	background: #fff;\
	border: 0 none;\
	border-left: 1px solid #dcdcdc;\
	cursor: pointer;\
	float: left;\
	height: 22px;\
	margin: 0;\
	padding: 0;\
	position: relative;\
	}\
	.ace_searchbtn:last-child,\
	.ace_replacebtn:last-child {\
	border-top-right-radius: 3px;\
	border-bottom-right-radius: 3px;\
	}\
	.ace_searchbtn:disabled {\
	background: none;\
	cursor: default;\
	}\
	.ace_searchbtn {\
	background-position: 50% 50%;\
	background-repeat: no-repeat;\
	width: 27px;\
	}\
	.ace_searchbtn.prev {\
	background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAFCAYAAAB4ka1VAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAADFJREFUeNpiSU1NZUAC/6E0I0yACYskCpsJiySKIiY0SUZk40FyTEgCjGgKwTRAgAEAQJUIPCE+qfkAAAAASUVORK5CYII=);    \
	}\
	.ace_searchbtn.next {\
	background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAFCAYAAAB4ka1VAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAADRJREFUeNpiTE1NZQCC/0DMyIAKwGJMUAYDEo3M/s+EpvM/mkKwCQxYjIeLMaELoLMBAgwAU7UJObTKsvAAAAAASUVORK5CYII=);    \
	}\
	.ace_searchbtn_close {\
	background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAcCAYAAABRVo5BAAAAZ0lEQVR42u2SUQrAMAhDvazn8OjZBilCkYVVxiis8H4CT0VrAJb4WHT3C5xU2a2IQZXJjiQIRMdkEoJ5Q2yMqpfDIo+XY4k6h+YXOyKqTIj5REaxloNAd0xiKmAtsTHqW8sR2W5f7gCu5nWFUpVjZwAAAABJRU5ErkJggg==) no-repeat 50% 0;\
	border-radius: 50%;\
	border: 0 none;\
	color: #656565;\
	cursor: pointer;\
	float: right;\
	font: 16px/16px Arial;\
	height: 14px;\
	margin: 5px 1px 9px 5px;\
	padding: 0;\
	text-align: center;\
	width: 14px;\
	}\
	.ace_searchbtn_close:hover {\
	background-color: #656565;\
	background-position: 50% 100%;\
	color: white;\
	}\
	.ace_replacebtn.prev {\
	width: 54px\
	}\
	.ace_replacebtn.next {\
	width: 27px\
	}\
	.ace_button {\
	margin-left: 2px;\
	cursor: pointer;\
	-webkit-user-select: none;\
	-moz-user-select: none;\
	-o-user-select: none;\
	-ms-user-select: none;\
	user-select: none;\
	overflow: hidden;\
	opacity: 0.7;\
	border: 1px solid rgba(100,100,100,0.23);\
	padding: 1px;\
	-moz-box-sizing: border-box;\
	box-sizing:    border-box;\
	color: black;\
	}\
	.ace_button:hover {\
	background-color: #eee;\
	opacity:1;\
	}\
	.ace_button:active {\
	background-color: #ddd;\
	}\
	.ace_button.checked {\
	border-color: #3399ff;\
	opacity:1;\
	}\
	.ace_search_options{\
	margin-bottom: 3px;\
	text-align: right;\
	-webkit-user-select: none;\
	-moz-user-select: none;\
	-o-user-select: none;\
	-ms-user-select: none;\
	user-select: none;\
	}";

function initNewNodeBox(editor) {
	var dom = ace.require("ace/lib/dom");
	var html = '<div class="ace_search right">\
	<button type="button" action="hide" class="ace_searchbtn_close"></button>\
	<div class="ace_replace_form">\
		<input class="ace_search_field" placeholder="nom du noeud" spellcheck="false"></input>\
		<button type="button" action="createElement" class="ace_replacebtn">Créer</button>\
	</div>\
	</div>'.replace(/>\s+/g, ">");
	dom.importCssString(searchboxCss, "ace_searchbox");
	var div = dom.createElement("div");
	div.innerHTML = html;
	var element = div.firstChild;
	editor.container.appendChild(element);
	var action = element.querySelector("[action=createElement]");
	var close = element.querySelector("[action=hide]");
	var input = element.querySelector(".ace_search_field");
	element.style.display = "none";
	var handleClick=function() {
		//console.log("handle click");
		var cursor = editor.selection.getCursor();
		//console.log("cursor=",cursor);
		var nodeName = editor.newNodeBox.input.value;
		
		
		if (nodeName!="") {
			if (editor.newNodeBox.data.caption=="NEW NODE") {
				var t = editor.session.getTextRange(editor.getSelectionRange());			
				editor.insert("<"+nodeName+">"+t+"</"+nodeName+">");
				
			} else {
				var t = editor.session.getTextRange(editor.getSelectionRange());			
				editor.insert(nodeName+'="" ');
				
			}
		}
		editor.newNodeBox.box.style.display = "none";
		editor.newNodeBox.input.value="";
		
		editor.newNodeBox.action.removeEventListener("click",editor.newNodeBox.handleClick);
		editor.focus();				
		editor.gotoLine(cursor.row+1, cursor.column + nodeName.length +2, true);
	};
	
	editor.newNodeBox={box:element,input:element.querySelector(".ace_search_field"),input:input,action: action,close:close,handleClick:handleClick};
	
	var handleKeyDown = function(e){
	  
		 if (e.which === 32)
				return false;
	  };
	  
  
   input.onkeydown=handleKeyDown;
   
   var handleKeyPress = function(e){
		if (!e) e = window.event;
		var keyCode = e.keyCode || e.which;
		
		
		if (keyCode == 13){
		  editor.newNodeBox.handleClick();
		  return false;
		}
	  };
	  
  input.addEventListener("keypress",handleKeyPress);
  
  var handleClose=function() {
		editor.newNodeBox.box.style.display = "none";
		//TODO : remettre le focus dans l'editeur
		editor.focus();
	};
	close.addEventListener("click",handleClose);
	
  
}	

function displayNewNode(editor,data) {
	
	editor.newNodeBox.data = data;
	editor.newNodeBox.action.addEventListener("click",editor.newNodeBox.handleClick);
	
	
  
	editor.newNodeBox.box.style.display = "";
	editor.newNodeBox.input.value="";
	editor.newNodeBox.input.focus();


}

	function initEditor(editor) {
		initNewNodeBox(editor);
		editor.on('mousedown',function(e) { // nativecontextmenu
			editor.newNodeBox.box.style.display = "none";
			
			if (e.domEvent.button != 2) return;
			editor.execCommand("startAutocomplete") ;
			
			
		});
					
						
					
var rhymeCompleter = {
		 				insertMatch: function(editor,data){
		 					//console.log("insertMatch data=",data);
		 					if (data.caption=="NEW NODE") {
		 						//console.log("demander le nom du nouveau noeud");
		 						//var config = ace.require("ace/config");
		 						//config.loadModule("ace/ext/newnodebox", function(e) {e.NewNode(editor)});
		 						displayNewNode(editor,data);
		 						/*
		 						editor.forEachSelection(function() {
			 	                    editor.insert(data.caption);
			 	                })
			 	                */
			 	                /*
		 						var t = editor.session.getTextRange(editor.getSelectionRange());
								editor.insert("<nouveau>"+t+"</nouveau>");
								*/ 
		 					} else if (data.caption=="NEW ATTRIBUTE") {		 						
		 						displayNewNode(editor,data);		 						 
		 					} else {
		 						//console.log("other");
		 					}
		 					// call the not overloaded insertMatch
		 					// editor.completer.insertMatch({value: data.caption})
		 					
		 				},
						getCompletions: function(editor, session, pos, prefix, callback) {
							//console.log("getCompletions");
							//console.log("prefix is",prefix);
							
							var TokenIterator = ace.require("ace/token_iterator").TokenIterator;
							
							var tokenizer = new TokenIterator(session, pos.row, pos.column);
							
							var s = getFullPath(editor.getSession().getMode(),tokenizer,session,TokenIterator) ;
							
							
							var nodes = s.slice(0);
							nodes = nodes.concat(editor.rootPath);
							
							var childs = getSchemaChildFromTags(getSchemaRootElements(currentSchema),nodes);
							
							var elems=[];
							var token = editor.session.getTokenAt(pos.row, pos.column);
							//console.log("token type=",token.type);
							if (token.type==="text.tag-whitespace.xml") {	
								elems.push( {caption: "NEW ATTRIBUTE",snippet:"xxsnip",completer:rhymeCompleter});
							} else {
								elems.push( {caption: "NEW NODE",snippet:"xxsnip",completer:rhymeCompleter});
							}
							
							if (childs!=null) {
								
								if (token.type=="text.tag-whitespace.xml") {									
									// nous sommes dans les attributs d'un tag
									// nous n'affichons donc que les attributs
									
									childs.map(function(c) { 
										if (c.node.localName == "attribute") {
											var n = c.node.getAttribute("name");
											
											var snippet=n+'="${1}"';
											elems.push( {caption: n, snippet: snippet});
										}
										
										
									});
								} else {
								
									childs.map(function(c) {
										if (c.node.localName == "element") {
											var n = c.node.getAttribute("name");									
											var snippet='<'+n;
											
											var childsOfChild = getSchemaAttributeAndChild(c.node);
											var index=1;
											childsOfChild.map(function(coc) {
												if (coc.node.localName == 'attribute') {
													snippet+=' '+coc.node.getAttribute("name")+'="${'+index+'}"';
													index++;
												}
											});
											
													
											//snippet +='>${'+index+'}</'+n+'>';
											snippet +='>${0:$SELECTION}</'+n+'>';
											elems.push( {caption: n, snippet: snippet});
										}
										
										
									});
								}
							
							}	// if child is null		
							//console.log("elems=",elems);
							callback("",elems ); // null au lieu de ""
							//return true;
						}
						/*
						,

			            insertMatch: function(editor, data) {
			            	console.log("insertMatch");
			                editor.forEachSelection(function() {
			                    editor.insert(data.caption);
			                })
			            }
			            */
					}
					editor.completers =[rhymeCompleter];
/*
 cssEditor.commands.on("afterExec", function(e){ 
        if (e.command.name == "insertstring"&&/^[\w\:.]$/.test(e.args)) { 
        	cssEditor.execCommand("startAutocomplete") 
     	} 
	  });
 */
/*
					editor.commands.on("afterExec", function(e){ 
					    console.log("afterExec",e.command.name);
					    if (e.command.name==="Return") {
					    	alert("xx");
					    }
					  });
					editor.commands.on("beforeExec", function(e){ 
					    console.log("beforeExec",e.command.name);
					    
					  });
*/

editor.commands.addCommand({
						name: 'myCommand',
						bindKey: {win: 'Ctrl-M',  mac: 'Command-M'},
						exec: function(editor) {
							console.log("control-M command");
							var t = editor.session.getTextRange(editor.getSelectionRange());
							editor.insert("<toto>"+t+"</toto>");
						}
					});	
					

}				

function getSchemaChildFromTags(fromNodes,tags) {
			
			// 1) get the root element schema
	
			var topTagName = tags.pop().tagName;
			
			var foundNode = null;
			for (var i=0;i<fromNodes.length;i++) {
				var n;
				if (fromNodes[i].node != null) {
					n = fromNodes[i].node;
					if (n.localName=="attribute") { // bypass attribute
						continue;
					}
				} else {
					n = fromNodes[i];
				}
				
				//console.log(topTagName,"compare with ",n);
				if (n.getAttribute("name") == topTagName) {
					foundNode =n;
					//console.log("found");
				}
				
			}
			
			if (foundNode == null) {
				
				return null;
			}
			
			
			var childs = getSchemaAttributeAndChild(foundNode);
			//console.log("childs is ",childs);
			if (tags.length == 0) {
				return childs;
			} else {
				return getSchemaChildFromTags(childs,tags);
			}
			
		}
		
function getFullPath(XMLMode,iterator,session,TokenIterator) {
			var tag;
			var stack = [];
			// TODO : si le tag est closing alors il n'est pas pris en compte !
			
			while (tag = XMLMode.foldingRules._readTagBackward(iterator)) {
				
                if (tag.selfClosing) {
					// do nothing
				} else if (tag.closing) {                   					
					removeUntilStartTag(XMLMode,iterator,tag);
					//iterator  = new TokenIterator(session, tag.start.row, tag.start.column);
					
                } else {		
					if (tag.tagName!="") {
						stack.push(tag);
					}
				}
                
                
            }
			return stack;
		}
		
		function removeUntilStartTag(XMLMode,iterator,untilTag) {
			
			var tag = null;
			while (tag = XMLMode.foldingRules._readTagBackward(iterator) ) {
				if (tag.closing) {                   					
					removeUntilStartTag(XMLMode,iterator,tag);
					//iterator  = new TokenIterator(session, tag.start.row, tag.start.column);
					
                } else if (tag.tagName == untilTag.tagName) {										
					return;
				} 
				
			/*
				if (tag.selfClosing) {
					// do nothing
				} else if (tag.closing) {                   					
					removeUntilStartTag(XMLMode,iterator,tag);
					//iterator  = new TokenIterator(session, tag.start.row, tag.start.column);
					
                } else if (tag.tagName == untilTag.tagName) {										
					return;
				} else {
				}
				*/
			}
		}	
