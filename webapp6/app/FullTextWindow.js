
Ext.define('Desktop.FullTextWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'fulltext-win',

    init : function(){
        this.launcher = {
            text: 'Filtre plein texte',
            iconCls:'icon-grid',
            idmenu:"requete"
        };
    },

    createWindow : function(){
		
        var desktop = this.app.getDesktop();
        var theapp = this.app;
           var win = desktop.createWindow({
        	   itemId: 'fulltext-win',
                title:'Filtre plein texte',
				y:0,
				
				x:$(window).width()*0.3,
                width:$(window).width()*0.4,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'border',
				/*
				defaults: {
					collapsible: true,
					split: true
					//splitterResize : true
					//,bodyStyle: 'padding:15px'
				},		
				*/
				execFromHisto(data) {
					
					var searchTerm = data.params.searchTerm;
					this.down("#searchTermField").setValue(searchTerm);
					
					this.loadTreemap("","",searchTerm,false);
					
				},
				items:[
					{
						collapsible: true,
						split: true,
						region:'north',
						xtype:'panel',						
						itemId:'treemap_panel',
						height:300,
						title:"treemap",
						frame:false,
						border: false, 
						html:'<div  name="treemap_x"/>',
						listeners:{
							afterrender:function() {
									var treemapId=this.id+"-treemap";
									$("#"+this.id).find("div[name='treemap_x']").attr("id",treemapId);
									var treemap = "#"+treemapId;
									
									this.up("window").treemap = treemap;
									var thewin = this.up("window");
									var mouseclickHandler = function(e,data) {
										var nodes = data.nodes;
										var ids = data.ids;
										
										
										var sl = nodes[0].id.split("/");
										var docuri = sl[0]+"/"+sl[1];
										sl.shift();
										sl.shift();
										var refnode = "/"+sl.join("/");
										
										thewin.loadFiche(docuri,refnode,thewin.searchTerm,nodes[0].nbresult);
										
									};
									var mousemoveHandler = function(e,data) {
										if (data.nodes[0] == undefined) return;
										
										var slash =data.ids[0].indexOf("/")+1; 
										this.title=removeQName(data.ids[0].substring(slash))+", "+data.nodes[0].nbresult+" résultat(s)";
										
									};
									/*
									 "colorStops":[
											{"val":0,  "color":"#00f"},
											{"val":0.01,  "color":"#070"},																
											{"val":1.0,  "color":"#f00"}
										]
									 */
									$(treemap).treemap({																					
										"labelsEnabled":false,		
										naColor: "#fff",
										"colorOption":0	,
										 "colorStops":[
														{"val":0,  "color":"#fff"},																												
														{"val":1.0,  "color":"#555"}
													]
									}).bind('treemapmousemove',mousemoveHandler)
										.bind('treemapclick',mouseclickHandler);
								
							},
							resize: function(){								
								var treemap = "#"+this.id+"-treemap";
								//console.log(this.getHeight());
								//console.log($(treemap).height());
								$(treemap).treemap("option","dimensions",[this.getWidth(),this.getHeight()-36]);
								
							}
						}
					},
					{
						region:'center',
						xtype:'panel',						
						itemId:'fulltext_panel',
						html:'<div style="height:100%;width:100%" name="editor">',
						aceEditor:null,
						listeners:{
							afterrender:function() {								
								$("#"+this.id).find("div[name='editor']").attr("id",this.id+"-editor");
								
								var langTools = ace.require("ace/ext/language_tools");
								editor = ace.edit(this.id+"-editor");
								this.up("window").editor = editor;
								
								editor.setOptions({
									enableBasicAutocompletion: false,
									enableSnippets: false,
									enableLiveAutocompletion: false
								});
								editor.setReadOnly(true);  
								editor.setTheme("ace/theme/twilight");
								var Mode = require('ace/mode/xml').Mode;
								var themode = new Mode();
								editor.getSession().setMode(themode);
								
								this.aceEditor = editor;
								//initEditor(editor);
								
								
							},
							resize: function(){
								var graph_id = this.id+"-editor";
								this.aceEditor.resize(true);
								
							}
						},
						dockedItems: [{
							xtype: 'toolbar',							
							dock: 'top',
							items: [ { xtype:'textfield',flex:1,fieldLabel:'chemin racine',itemId:"node_path",editable:false}]
						}]
					}
				],
				
				currentPage:1,
				totalPage:1,
				nb_result_per_page:1,
				loadFiche:function(docuri,refnode,searchTerm,nbResult) {
					var me = this;
					var editorPanel = this.down("#fulltext_panel");
					editorPanel.setLoading(true);
					if (nbResult==0) {
						searchTerm = "";
					}
					$.post('/service/commands', {cmd:'getFullTextFiche',docuri:docuri,refnode:refnode,view_mode:this.currentViewMode,search:searchTerm}, function(data, textStatus) {																						
							var sl = docuri.indexOf("/");
							me.down("#node_path").setValue(docuri.substring(sl+1)+removeQName(refnode));
							me.displayDoc(data.result);							
							editorPanel.setLoading(false);
						});
				},
				loadTreemap:function(docuri,refnode,searchTerm,addToHisto) {
					this.docuri = docuri;
					this.refnode = refnode;
					
					if (addToHisto) {
						
						theapp.addToHisto({from:"fulltext",type:"query",params:{searchTerm:searchTerm},timestamp:null}); //{from:"modalite",type:"query",params:{},timestamp:null}
					}
					this.searchTerm = searchTerm;													
					this.setLoading(true);
					var me = this;
					$.post('/service/commands', {cmd:'getFullTextTreemap',search:searchTerm,docuri:docuri,refnode:refnode}, function(response, textStatus) {
							var data = response.result;
						if (data.children.length == 1) {
							// 1 seul fils donc nous demandons une recherche en profondeur +1
							//local43/1.xml/Q{}prosop[1]
							var sl = data.children[0].id.split("/");
							var docuri2 = sl[0]+"/"+sl[1];
							sl.shift();
							sl.shift();
							var refnode2 = "/"+sl.join("/");
							
							me.loadTreemap(docuri2,refnode2,searchTerm,false);
						} else {
							$(me.treemap).treemap("option","nodeData",data);
							me.down("#nbResultField").setText(response.total+" résultat(s)");
							me.editor.setValue("");							
							me.down("#node_path").setValue("");
							me.setLoading(false);
						}
					});
				},
				resetPagination:function(total) {					
					this.totalPage = total/this.nb_result_per_page;
					var start = 1;
					if (total==0) {
						start = 0;
					}
					this.down("#pages").setData(start+"/"+this.totalPage);
				},
				nextPage:function() {
					if (this.currentPage == this.totalPage) return;
					var me = this;
					me.setLoading(true);
					$.post('/service/commands', {cmd:'getFullText',view_mode:this.currentViewMode,search:this.searchTerm,start:this.currentPage+1,nbResult:1}, function(data, textStatus) {															
							me.displayDoc(data);
							me.currentPage = me.currentPage+1;
							me.down("#pages").setData(me.currentPage+"/"+me.totalPage);
							me.setLoading(false);
						});
				},
				prevPage:function() {
					if (this.currentPage == 1) return;
					var me = this;
					me.setLoading(true);
					$.post('/service/commands', {cmd:'getFullText',view_mode:this.currentViewMode,search:this.searchTerm,start:this.currentPage-1,nbResult:1}, function(data, textStatus) {															
							me.displayDoc(data);
							me.currentPage = me.currentPage-1;
							me.down("#pages").setData(me.currentPage+"/"+me.totalPage);
							me.setLoading(false);
						});
				},
				firstPage:function() {
					if (this.currentPage == 1) return;
					var me = this;
					me.setLoading(true);
					$.post('/service/commands', {cmd:'getFullText',view_mode:this.currentViewMode,search:this.searchTerm,start:1,nbResult:1}, function(data, textStatus) {															
							me.displayDoc(data);
							me.currentPage = 1;
							me.down("#pages").setData(me.currentPage+"/"+me.totalPage);
							me.setLoading(false);
						});
				},
				lastPage:function() {
					if (this.currentPage == this.totalPage) return;
					var me = this;
					me.setLoading(true);
					$.post('/service/commands', {cmd:'getFullText',view_mode:this.currentViewMode,search:this.searchTerm,start:this.totalPage,nbResult:1}, function(data, textStatus) {															
							me.displayDoc(data);
							me.currentPage = me.totalPage;
							me.down("#pages").setData(me.currentPage+"/"+me.totalPage);
							me.setLoading(false);
						});
				},
				currentViewMode:'xml',
				setCurrentViewMode:function(viewMode) {
					this.currentViewMode = viewMode;
					var me = this;
					me.setLoading(true);
					if (viewMode=='xml') {
						var Mode = require('ace/mode/xml').Mode;
						var XMLMode = new Mode();
						this.editor.getSession().setMode(XMLMode);
					} else {						
						this.editor.getSession().setMode("ace/mode/html");
					}
					$.post('/service/commands', {cmd:'getFullText',view_mode:this.currentViewMode,search:this.searchTerm,start:this.currentPage,nbResult:1}, function(data, textStatus) {																						
							me.displayDoc(data);							
							me.setLoading(false);
						});
				},
				displayDoc: function(data) {
					this.editor.getSession().setAnnotations([]);
					var markers = this.editor.getSession().getMarkers();
					for (var k in markers) {
						if (markers[k].clazz == "search_term_found") {
							this.editor.getSession().removeMarker(k);
						}
					}
					
					if (data==="" ) {
						this.editor.setValue("");					
						Ext.Msg.alert('Erreur', 'aucun résultat');
						return;
					}
					
					var doc="";	
					if (this.currentViewMode == 'xml') {
						doc = data.substring(10,data.length-11); // on enleve le tag "<result>"						
					} else {
						doc = data;
					}
					
					
					this.editor.scrollToLine(1, true, true, function () {});
					this.editor.gotoLine(1, 1, true);
					
					this.editor.setValue(doc);
					var Range = ace.require("ace/range").Range
					var l = '<clioxml_mark>'.length;
					var l2 = '</clioxml_mark>'.length;
					
					var finished = false;
					var annos={};
					while (!finished) {
						this.editor.gotoLine(1, 1, true);
						this.editor.find('<clioxml_mark>');						
						var c = this.editor.selection.getCursor(); // cursor = {columns:xx,row:yy}
						
						
						if (c.row == 0 && c.column == 1) {
							finished = true;
						} else {
							
							if (annos[""+c.row] == null) {								
								annos[""+c.row] = [];
							}
							annos[""+c.row].push(c.column-l);							  
							  
							this.editor.replace('');
							this.editor.find('</clioxml_mark>');
							var c2 = this.editor.selection.getCursor();
							this.editor.replace('');
							var range = new Range(c.row, c.column-l, c2.row, c2.column-l2);
							this.editor.getSession().addMarker(range,"search_term_found", "text");
						}
					}
					
					
					var annotations=[];
					var minRowIndex = null;
					for (var k in annos) {	// attention k est non ordonnées (				
						var a = annos[k];
						var r = parseInt(k);
						
						if (minRowIndex == null) {
							minRowIndex = annotations.length;
						} else if (annotations[minRowIndex].row>r) { // si le row actuelle est plus petit que le row de l'annotation "elue" alors on prends l'actuel
							minRowIndex = annotations.length;
						}
						annotations.push({
							column:a[0],
							row: r,
							text: a.length+" résultat(s)",
							type: "error" // also warning and information, error
							 });
					}
					
					if (annotations.length>0) {
						this.editor.getSession().setAnnotations(annotations);
						this.editor.scrollToLine(annotations[minRowIndex].row+1, true, true, function () {});
						this.editor.gotoLine(annotations[minRowIndex].row+1, annotations[minRowIndex].column, true);
					};
							
				},
				listeners: {
					afterrender:function() {	
						if (theapp.user.credential.readwrite==false) {
							var saveButton = this.down("#saveButton");
							saveButton.setDisabled(true);
						} 
						/* todo : dans le change du search term
						var me = this;
						var start= 1;
						me.setLoading(true);
						$.post('/service/commands', {cmd:'getFullText',view_mode:this.currentViewMode,search:this.searchTerm,start:start,nbResult:1}, function(data, textStatus) {	
							me.resetPagination(data.total);
							
							me.displayDoc(data);
							me.setLoading(false);
						});
						*/
					}
				},
				
				dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
									
									{
										xtype:'textfield',flex:1,itemId:'searchTermField',fieldLabel:'Search :'
										
									},
									{
										icon: "theme/neptune/images/magnify.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													var searchTerm = this.up("window").down("#searchTermField").getValue();
													var treemap = this.up("window").treemap;
													
													
													var me = this.up("window");
													
													me.loadTreemap("","",searchTerm,true);
													
												}
											}
									},
									{
										xtype:'label',itemId:'nbResultField',text:'--- Résultats'										
									},
									{
										text:"xquery",
										listeners:{								
											'click':function(button) {
												// ouverture de l'éditeur xquery pour les modalites
												var url="/service/commands?cmd=getXQueryFullText";
												var searchTerm = this.up("window").down("#searchTermField").getValue();
												$.post(url,{searchTerm:searchTerm},function(response) {
													var module = new Desktop.XQueryEditorWindow();
													module.app = theapp;
													var xquery=response;		
													var editorWindow = module.createWindow(xquery);
													editorWindow.show();
												});
												
												
											}
										}
									},
									{
										text: 'Action',                      
										menu: {
											xtype: 'menu',                          
											items: [
												{
													text:"export",
													listeners:{								
														'click':function(button) {
															var f = $("#formDownload");				
															f.empty();
															var w = button.up("window");
															f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("exportFicheFullTextTreemap"));			
															f.append($("<input>").attr("type", "hidden").attr("name", "docuri").val(w.docuri));
															f.append($("<input>").attr("type", "hidden").attr("name", "refnode").val(w.refnode));
															f.append($("<input>").attr("type", "hidden").attr("name", "search").val(w.searchTerm));
															f.append($("<input>").attr("type", "hidden").attr("name", "format").val("xml"));
															f.submit();
															
														}
													}
												},
												{
													text: 'Enregistrer',	
													itemId:'saveButton',
													listeners: {
														click:function(button) {
															Ext.Msg.prompt("Enregistrement", "Nom pour cette requête :", function(btnText, sInput){
												                if(btnText === 'ok'){
												                	var w = button.up("window");
																	var params = {searchTerm:w.searchTerm};
												                    
												                    var url="/service/commands?cmd=saveQuery";
																	$.post(url,{from:"fulltext",type:"query",name:sInput,params:JSON.stringify(params)},function(response) {
																		console.log(response);
																	});
												                }
												            }, this);
																
														}
													}
												} // bouton enregister
												]//items
											} // menu
									}
									/*
									,
									{
										icon: "theme/neptune/images/grid/page-first.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													this.up("window").firstPage();
												}
											}
									},
									{
										icon: "theme/neptune/images/grid/page-prev.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													this.up("window").prevPage();
												}
											}
									},
									{
										xtype:'textfield',
										//fieldLabel:'document',
										value:'1/10',
										labelWidth:45,
										grow:true,
										itemId:'pages'
									},
									{
										icon: "theme/neptune/images/grid/page-next.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													this.up("window").nextPage();
												}
											}
									},
									{
										icon: "theme/neptune/images/grid/page-last.png",										
										xtype: 'button',
										listeners: {
												click:function(button) {
													this.up("window").lastPage();
												}
											}
									},
										
										{
										text: 'Actions',                      
										menu: {
											xtype: 'menu',                          
											items: [{
													text: 'Vue',
													menu : [ 
															{
																xtype: 'radiogroup',
																columns: 1,
																vertical: true,
																items: [
																	{ 
																		boxLabel: 'XML', 
																		name: 'vue', 
																		inputValue: 'xml' ,
																		checked:true
																	},
																	{ 
																		boxLabel: 'Text', 
																		name: 'vue', 
																		inputValue: 'text' 
																	}           
																],
																listeners: {
																	change: function (field, newValue, oldValue) {
																			
																			var win = field.up("window"); 
																			win.setCurrentViewMode(newValue.vue);
																			field.up("menu").up("menu").hide();																		
																	}
																}
															}
														]
												}]                          
										}
									} */
									]
								}
								/*
								,
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
										{ xtype:'textfield',flex:1,fieldLabel:'chemin racine',itemId:"node_path",editable:false}
									]
								}
								*/
								]
								
               
            });
        
        return win;
    }
});
