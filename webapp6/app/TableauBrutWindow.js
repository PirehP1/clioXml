/*
Ext.define('ColModelSC',{
        	extend: 'Ext.data.Model',
        	fields: [{name:"value"},{name:'clioxml_modify'},{name:'clioxml_original_value'}]        	
         });
         
        
        
        
        Ext.define('ColModelC', (function () {  
          'use strict';
          return {
            extend: 'Ext.data.Model',
            requires: [ 'ColModelSC' ],
            hasMany: [
              { model: 'ColModelSC', name: 'getSubCols',associationKey:'subcols'}               
            ]
          };
        }()));
        
        Ext.define('ColModelR', (function () {  
          'use strict';
          return {
            extend: 'Ext.data.Model',
             requires: [ 'ColModelC' ],
            hasMany: [
              { model: 'ColModelC', name: 'getCols',associationKey:'cols'}               
            ]
          };
        }()));
			*/								
// PS : le tri ne fonctionne pas (avec un click sur la colonne) car les models sont dynamiques (pas de type)
Ext.define('Desktop.TableauBrutWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'tableaubrut-win',

    init : function(){
        this.launcher = {
            text: 'Individus/caractères',
            iconCls:'icon-grid',
            idmenu:"tableau"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
		var theapp = this.app;
           var win = desktop.createWindow({
                itemId: 'brut-win',
                title:'Tableau Individus/caractères',
                width:$(window).width()*0.4,
				y:0,
				x:$(window).width()*0.3,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
				app:theapp,
				gotoPage:function(p) {
                	this.currentPage = p;
                	var grid = this.down("grid");
                	var start = (this.currentPage-1)*this.nb_result_per_page+1;
                	grid.loadTableauBrut_start(false,start);
                },
                currentPage:1,
				totalPage:1,
				nb_result_per_page:20,
				
                firstPage:function() {
                	if (this.currentPage == 1) return;
					this.gotoPage(1);
                },
                lastPage:function() {
                	if (this.currentPage == this.totalPage) return;					
					this.gotoPage(this.totalPage);
                },
                
				nextPage:function() {
					if (this.currentPage == this.totalPage) return;					
					this.gotoPage(this.currentPage+1);
				},
				prevPage:function() {
					if (this.currentPage == 1) return;
					this.gotoPage(this.currentPage-1);
				},
				execFromHisto(data) {
					
					
					var grid = this.down("grid");
					var cc = $.extend(true, [], data.params.clioxml_columns);
					grid.clioxml_columns =  cc; //getFullPath_from_array(els);
					
					
					grid.current_filtreId = data.params.filtreId;// reinit it
					
					var combo = this.down("#choix_filtre");
					combo.suspendEvents();
					combo.setValue(grid.current_filtreId);
					combo.resumeEvents(true); // true = discardQueuedEvents
					
					grid.loadTableauBrut(false);
				},
				listeners:{
					
					afterrender:function() {
						var me = this;
						this.addTool({
							  type:'refresh',
							  hidden:true,
							  handler: function() {
								var tab = me.down("grid");
								tab.loadTableauBrut(false);
								
								me.tools["refresh"].hide();
							  },
							  scope:me
							});
							
						
						
						var showRefresh = function() {me.tools["refresh"].show();};
						var showRefreshFiltre = function(source,modifiedFiltreId) {
									var combo = me.down("#choix_filtre");
									combo.getStore().reload();
									var current_filtreId = combo.getValue();
									if (current_filtreId!=-1 && current_filtreId == modifiedFiltreId) {
										me.tools["refresh"].show();
									}							
																		
								};							
						this.addListener({'beforeclose':function() {
							me.app.removeListener("codageUpdated",showRefresh); 
							me.app.removeListener("filtreUpdated",showRefreshFiltre);
						}});
						this.app.addListener({'codageUpdated':showRefresh});
						this.app.addListener({'filtreUpdated':showRefreshFiltre});
						//this.app.addListener({'codageUpdated':function() {me.tools["refresh"].show();}});
					}
					
				},
				header:{
					xtype: 'header',
					titlePosition: 0,
					defaults: {
						padding: '0 0 0 0'
					},
					items: [
					{
						xtype:'combo',
						 displayField:'name',
						 valueField:'id',											 
						 itemId:"choix_filtre",
						 editable:false,
						 
						 flex:1,
						 store : new Ext.data.Store({
								autoLoad:true,
							fields: ['id','name'],
							proxy: {
								type: 'ajax',
								url: '/service/commands?cmd=listFiltre',
								reader: {
									type:'json',
									rootProperty:"filtres"
								}
							},
							 
								 
							  
						})
					}
					] // fin items
				}	, // fin header
                items: [
                    {
						current_filtreId:-1,
                        border: false,
						xtype:'grid',
						columnLines: true,
						clioxml_columns:[],
						selModel:'cellmodel',
						
						selModelXX: {
							
							mode: 'MULTI',
							allowDeselect: true,
							listeners:{
								
								select:function ( selModel, record, row, columnIndex, eOpts ) {
										
										return false;
										/*
										console.log("selModel select");
										console.log(row,columnIndex);
										var r = selModel.view.mydata[row];
										
										var col = r.cols[columnIndex];
										console.log("col=",col);
										
										record.set('modalite',col.subcols[0].value); 
									*/
								}
								
								
							}
						},
						
						
						viewConfig: {
							getRowClass: function( record, index ) {									
									var r = this.mydata[index];
									
									if (r.rownum%2==0) {
										return "tableau_brut_ligne";
									} 
								return "";
							}	,
							//current_selection:[],
							current_selection_target:[],
							current_selection_items:[],
							current_col_index_selection:null,
							
							removeSelection:function() {
								$.each(this.current_selection_target,function(i,v) {
									Ext.get(v).removeCls('black');
								});
								this.current_selection_target = [];
								this.getSelectionModel().selected.items = [];
								this.current_selection_items = [];
							},
							
							listeners:{
								
								cellclick : function(view, cell, colIndex, record,row, rowIndex, e) {
										
									//return;
									//  test if control key or shift key
									
										if (!e.parentEvent.ctrlKey) {
											return;
										}
										
										if (colIndex== 0) {
											return ; // on ne peut pas selectionner la premiere colonne (l'index), ce n'est pas une variable
										}
										
										var tableau_brut = view.up("grid");
										var columns=[{"index":""}]; 
										for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {													
											flattenColumn(columns,tableau_brut.clioxml_columns[i].cloneNode,tableau_brut.clioxml_columns[i].originalNode);
										}	
												
										
										// sommes nous dans une colonne finale ?
										if (columns[e.position.colIdx].type == null || columns[e.position.colIdx].type == "") {
											// pas de type donc on 
											Ext.Msg.show({
												title: 'Attention',
												msg: "seul les noeuds finaux peuvent être recodés",
												buttons: Ext.Msg.OK,
												icon: Ext.Msg.WARNING
											});
											return;
										}
										
										
										
										if (view.current_col_index_selection!=colIndex) {
											// changement de selection de colonne (donc de variable)
											// 1) nous effacons les selections precedentes
											view.removeSelection();
											/*
											$.each(view.current_selection_target,function(i,v) {
												Ext.get(v).removeCls('black');
											});
											view.current_selection_target = [];
											view.getSelectionModel().selected.items = [];
											*/
										}
										
										
										view.current_col_index_selection = colIndex;
										var r = view.mydata[rowIndex];										
										var col = r.cols[colIndex];	
										/*
										if (col.subcols.length>1) {
											return; // not a final col
										}
										*/										
										//console.log("col is",col);
										var value = col.value;
										
										var index = view.current_selection_target.indexOf(e.target);
										if (index >-1) { // cell deja selectionné
											// nous devons deselectionner cette case
											Ext.get(e.target).removeCls('black');
											view.current_selection_target.splice(index,1);											
											view.getSelectionModel().selected.items.splice(index,1);
											view.current_selection_items.splice(index,1);
											
										} else {
											
											Ext.get(e.target).addCls('black');
											view.current_selection_target.push(e.target);	
											
											//view.select(value, true, true);
											var m = Ext.create("SimpleModaliteModel",{modalite:value});
											view.getSelectionModel().selected.items.push(m); // ok cela fonctionne pour ajouter dans la selection !!
											view.current_selection_items.push(m);
										}
										
										tableau_brut.current_path = columns[e.position.colIdx].path;
										tableau_brut.current_path_type = columns[e.position.colIdx].type;
										
										
								}
							},
                            stripeRows: true,
							
							//enableLocking : true,
							copy:true,
							plugins: {
								ptype: 'gridviewdragdrop',
								dragGroup: 'modaliteDrop'
							}
							
                        },
                        /*
						plugins: [
                            'bufferedrenderer',
                            {
                                xclass: 'Ext.grid.plugin.RowEditing',
                                //clicksToMoveEditor: 1,
                                autoCancel: false
                            }
                            
                        ],*/
                        bufferedRenderer:true,	
                        dockedItems: [
                  					{
                  						xtype: 'toolbar',
                  						dock: 'top',
                  						items: [
                  							
                  							
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
                  								value:'',
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
                  								xtype:'combo',
                  								
                  								fieldLabel: 'Résultat par page :', 				 
                  								 width:170,
                  								 editable:false,
                  								 
                  								 //flex:1,
                  								 store : [10,20,50,100],
                  								value: 50,
                  								listeners:{
                  									select: function(combo, record, index) {
                  								      //write cool code here
                  										
                  										var w = combo.up("window");
                  										w.nb_result_per_page = record[0].raw[0];
                  										
                  										w.gotoPage(1);
                  										
                  								    },
                  									onChange:function(combo, newValue, oldValue, eOpts) {
                  										alert("onchange");
                  										/*
                										if (newValue!=me.current_filtreId) {
                											me.current_filtreId = newValue;	
                											me.loadTableauBrut(true);
                										}
                										*/
                									}
                  								}
                  							},
                  							{
                  								xtype:'label',                  								
                  								value:'',
                  								
                  								width:100,
                  								itemId:'total_field'
                  							}
                  							
                  						]
                  					}
                  					
                  					
                  					
                                  ], // dockeritems

						tbar: [
							{xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'Ajouter une colonne',itemId:"newCol",editable:false,emptyText:'coller un noeud xml'}
							,
							{
								text: 'Action',                      
								menu: {
									xtype: 'menu',                          
									items: [{
												text: 'export CSV',					
												listeners: {
													click:function(button) {
														var f = $("#formDownload");				
														f.empty();
														var tableau_brut = button.up('window').down("grid");
														var cols=[];
														var subcols=[];	
														indexCol=1;											
														for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {
															cols.push(getFullPathNS_from_array(schemaNode_to_array2(tableau_brut.clioxml_columns[i].originalNode))); 
															subcols.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
														}
																
																
														f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadTableauBrut"));			
														f.append($("<input>").attr("type", "hidden").attr("name", "colonnes").val(JSON.stringify(cols)));
														f.append($("<input>").attr("type", "hidden").attr("name", "subcols").val(JSON.stringify(subcols)));
														f.append($("<input>").attr("type", "hidden").attr("name", "filtreId").val(tableau_brut.current_filtreId));
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
											                    //console.log("name of query : ",sInput);
											                   
											                    var tableau_brut = button.up('window').down("grid");
																var cols=[];
																var subcols=[];	
																indexCol=1;											
																for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {
																	cols.push(getFullPathNS_from_array(schemaNode_to_array2(tableau_brut.clioxml_columns[i].originalNode))); 
																	//subcols.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
																}
																	
																var combo = button.up("window").down("#choix_filtre");
																var current_filtreId = combo.getValue();
																
																var filtreName = "";
																if (current_filtreId!=-1) {
																	filtreName = combo.getStore().findRecord('id',current_filtreId).data.name;
																}
																	
																var params = {colonnes:cols,filtreId:current_filtreId,filtreName:filtreName}; //subcols:subcols
											                    
											                    var url="/service/commands?cmd=saveQuery";
																$.post(url,{from:"tableau brut",type:"query",name:sInput,params:JSON.stringify(params)},function(response) {
																	console.log(response);
																});
											                }
											            }, this);
														/*
														var f = $("#formDownload");				
														f.empty();														
																
														f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadTableauBrut"));			
														f.append($("<input>").attr("type", "hidden").attr("name", "colonnes").val(JSON.stringify(cols)));
														f.append($("<input>").attr("type", "hidden").attr("name", "subcols").val(JSON.stringify(subcols)));
														f.append($("<input>").attr("type", "hidden").attr("name", "filtreId").val(tableau_brut.current_filtreId));
														f.submit();
														*/	
													}
												}
											},
											{
												text: 'export Textometrie',					
												listeners: {
													click:function(button) {
														var tableau_brut = button.up('window').down("grid");
														var cols=[];
														var subcols=[];	
														indexCol=1;											
														for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {
															cols.push(getFullPathNS_from_array(schemaNode_to_array2(tableau_brut.clioxml_columns[i].originalNode))); 
															subcols.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
														}
														//console.log("cols",cols);
														//console.log("subcols",JSON.stringify(subcols));
														console.log("subcols=",subcols);
														var curwin = Ext.create('Desktop.ExportTextometrieModal',{cols :cols,subcols:subcols,filtreId:tableau_brut.current_filtreId});																													
														curwin.show();	
													}
												}
											}
										
										] //items                         
								} // menu
							} // text : export
							,{
								text:"xquery",
								listeners:{								
									'click':function(button) {
										// ouverture de l'éditeur xquery 
										var tableau_brut = button.up('window').down("grid");
										var cols=[];
										var subcols=[];	
										indexCol=1;											
										for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {
											cols.push(getFullPathNS_from_array(schemaNode_to_array2(tableau_brut.clioxml_columns[i].originalNode))); 
											subcols.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
										}
										var w = button.up("window");
										var url="/service/commands?cmd=getXQueryTableauBrut";
										$.post(url,{colonnes:JSON.stringify(cols),start:1,nbResult:w.nb_result_per_page,subcols:JSON.stringify(subcols),filtreId:tableau_brut.current_filtreId},function(response) {
											var module = new Desktop.XQueryEditorWindow();
											module.app = theapp;
											var xquery=response;		
											var editorWindow = module.createWindow(xquery);
											editorWindow.show();
										});
										
										
									}
								}
							}	// menu xquery
							/*
							{
								text:"export",
								listeners:{
									
									'click':function(button) {
										var f = $("#formDownload");				
										f.empty();
										var tableau_brut = button.up('window').down("grid");
										var cols=[];
										var subcols=[];	
										indexCol=1;											
										for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {
											cols.push(getFullPathNS_from_array(schemaNode_to_array2(tableau_brut.clioxml_columns[i].originalNode))); 
											subcols.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
										}
												
												
										f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadTableauBrut"));			
										f.append($("<input>").attr("type", "hidden").attr("name", "colonnes").val(JSON.stringify(cols)));
										f.append($("<input>").attr("type", "hidden").attr("name", "subcols").val(JSON.stringify(subcols)));
										f.append($("<input>").attr("type", "hidden").attr("name", "filtreId").val(tableau_brut.current_filtreId));
										f.submit();					
									}
								}
							} // text:export
							*/
						]	,
						collapseColumn:function (current_node) {
							current_node.data.expanded = false;
							
						},
						removeColumn:function (current_node) {
							var index = -1;
							for (var i=0;i<this.clioxml_columns.length;i++) {
								var c = this.clioxml_columns[i];
								if (c.cloneNode == current_node) index = i;
							}
							if (index!=-1) {								
								this.clioxml_columns.splice(index, 1);
							}
							
						},
						expandColumn:function (current_node) {
							console.log("expandColumn");
							current_node.data.expanded = true;
							
							if (current_node.childNodes.length>0) {
								console.log("length>0");
								return; // le noeud a déjà été 'parsé'
							}
							var elementStructure = getSchemaElementStructure(current_node.data.schemaNode);						
							console.log("elementStructure=",elementStructure);
							if (elementStructure.localName =='complexType' ) {
								// start with attributes
								$.each(elementStructure.childNodes,function(key,val) {
									if (val.localName == 'attribute') {										
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
										//node.appendChild(n);									
										//current_node.data.schemaNode.appendChild(n);
										current_node.appendChild(n);
									} 
								});
								
								$.each(elementStructure.childNodes,function(key,val) {
									if (val.localName != 'attribute') { // could be sequence,choice,element or simpleContent
										processSequenceOrChoiceOrElement(val,current_node);									
									}
								});
							} else {
													console.log("onclick : elementStructure "+elementStructure.localName+" not implemented TODO");
												}	
						},
						cloneParentNode:function(n) {
							if( n == null) {
								return null;
							} else { 
								/*
								 var cs = [];
								   if (n.childNodes!=null) {
									   for (var i=0;i<n.childNodes.length;i++) {
										   cs.push(this.cloneNode(n.childNodes[i]));
									   }
									}
									*/
								var data={leaf:n.data.leaf,expanded:n.data.expanded,name:n.data.name,type:n.data.type,schemaNode:Ext.clone(n.data.schemaNode)};
							   return {data:data,childNodes:[],parentNode:this.cloneParentNode(n.parentNode)} ; /* ,childNodes:cs */
							}
						},
						cloneNode:function(n) {
							if( n === null) {
								return null;
							} else { 
							   var cs = [];
							   if (n.childNodes!=null) {
								   for (var i=0;i<n.childNodes.length;i++) {
									   var x = this.cloneNode(n.childNodes[i]);
									   if (x!=null) {
										   cs.push(x);
									   }
								   }
								}
							   if (cs.length == 0) {
								   cs = null;
							   }
							   /*
							   var s2 = null;
							   if (n.parentNode!=null) {
								   var s = JSON.stringify(JSON.decycle(n.parentNode));
								   var s2 = JSON.parse(s);
							   }
							   */
							   console.log("n.data.name=",n.data.name);
							   var data={leaf:n.data.leaf,expanded:n.data.expanded,name:n.data.name,type:n.data.type,schemaNode:Ext.clone(n.data.schemaNode)};
							   return {data:data,childNodes:cs,parentNode:this.cloneParentNode(n.parentNode)}; //Ext.clone(n.data) this.cloneNode(n.parentNode)
							}
						},
						getQueryParams:function() {
							var combo = this.up("window").down("#choix_filtre");
							var current_filtreId = combo.getValue();
							
							var filtreName = "";
							if (current_filtreId!=-1) {
								filtreName = combo.getStore().findRecord('id',current_filtreId).data.name;
							}
							
							var cc2 = $.extend(true, [], this.clioxml_columns);
							/*
							var cc=[];
							for (var i=0;i<cc2.length;i++) {
								var cc3=cc2[i];
								var c = {};
								//c.originalNode = {parentNode:this.cloneParentNode(cc3.originalNode),childNodes:cc3.originalNode.childNodes,data:cc3.originalNode.data};
								//c.cloneNode = {parentNode:cc3.cloneNode.parentNode,childNodes:cc3.cloneNode.childNodes,data:cc3.cloneNode.data};
								c.originalNode =  this.cloneNode(cc3.originalNode);
								c.cloneNode = this.cloneNode(cc3.cloneNode);
								cc.push(c);
							}
							console.log("azerty2",cc2);
							console.log("azerty",cc);
							
							
							//var boardJSON = JSON.stringify(JSON.decycle(cc2));
							//var cc =  JSON.retrocycle($.parseJSON(boardJSON));
							*/
							return {clioxml_columns:cc2,filtreName:filtreName,filtreId:current_filtreId};
						},
						
						loadTableauBrut:function(addToHisto) {
								this.loadTableauBrut_start(addToHisto,1);
						},
						loadTableauBrut_start:function(addToHisto,start) {
							var tableau_brut = this;
							if (tableau_brut.clioxml_columns.length == 0) {
								return;
							}							
							if (addToHisto) {
								var params = this.getQueryParams();
								theapp.addToHisto({from:"tableau brut",type:"query",params:params,timestamp:null}); //{from:"modalite",type:"query",params:{},timestamp:null}
							}
							this.getView().removeSelection();
							tableau_brut.setLoading(true);
											var cols=[];
											var subcols=[];	
											indexCol=1;											
											for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {
												cols.push(getFullPathNS_from_array(schemaNode_to_array2(tableau_brut.clioxml_columns[i].originalNode))); 
												subcols.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
											}
											
																				
											
											
											var w = tableau_brut.up("window");	
											$.post('/service/commands?cmd=getTableauBrut',{colonnes:JSON.stringify(cols),start:start,nbResult:w.nb_result_per_page,subcols:JSON.stringify(subcols),filtreId:this.current_filtreId},function(data, textStatus, jqXHR ) { 
												tableau_brut.setLoading(false);
												tableau_brut.up("window").tools["refresh"].hide();
												if (start == 1) {
													
													var total = parseInt($(data).find("total")[0].textContent);
													w.currentPage = 1;
													w.totalPage = Math.floor( total/ w.nb_result_per_page);
													if ((total % w.nb_result_per_page)>0) {
					                                	w.totalPage = w.totalPage+1;
					                                }
													w.down("#total_field").setData(total+" fiche(s) trouvée(s)");
												}
												w.down("#pages").setData(w.currentPage+"/"+w.totalPage);
												
												/*
												var xx = (new XMLSerializer()).serializeToString(data);
												console.log(xx);
												*/
												var rownum = (w.currentPage-1)*w.nb_result_per_page;
												var d=[];
												var d2=[];  // nous créons quand même un 'faux' tableau contenant seulement le nombres de lignes nécessaires
												
												$.each($(data).find("rows").children(),function(i,arow) {  // arow = <r>                                         	   
													var nb_col = $(arow).children().length;
												  var nb_sc = 0;
												  rownum++;
                                            	   $.each($(arow).children(),function (j,acol) { // les acol = <c>
														var count_sc = $(acol).children().length; // length ou count
														if (count_sc>nb_sc) {
															nb_sc = count_sc;
														}
														
                                            	   });
												   // maintenant on remplis le tableau 1 <r> -> plusieurs ligne dans the rows
												   var therows = new Array(nb_sc);
												   for (var i=0;i<nb_sc;i++) {
													   therows[i] = {"cols":new Array(nb_col),rownum:rownum};
													   for (var j=0;j<nb_col;j++) {
														   therows[i].cols[j] = "";
													   }
												   }
												   
												   
												    $.each($(arow).children(),function (current_col,acol) { // les acol = <c>
														$.each($(acol).children(),function (current_ligne,sc) { // les acol = <sc>	
															therows[current_ligne].rownum=rownum;
															therows[current_ligne].cols[current_col] = {value:$(sc).text(),clioxml_modify:$(sc).attr("clioxml:node__pmids"),clioxml_original_value:$(sc).attr("clioxml:node__oldvalue")};
														});
														
													});
													d2 = d2.concat( therows);
													for (var i=0;i<therows.length;i++) {
														d.push({}); 
													}
												   /*
												   var cols=[];													   
														var subcols=[];                                                	    
                                                	    $.each($(acol).children(),function (k,asubcol) {            	                                                        	        
																//subcols.push({value:$(asubcol).text(),clioxml_modify:$(asubcol).attr("clioxml_modify"),clioxml_original_value:$(asubcol).attr("clioxml_original_value")});
																subcols.push({value:$(asubcol).text(),clioxml_modify:$(asubcol).attr("clioxml:node__pmids"),clioxml_original_value:$(asubcol).attr("clioxml:node__oldvalue")});
                                                	    });
														cols.push({subcols:subcols});
														d.push({cols:cols});
														d2.push({}); 
												   */
												   //d.push({cols:cols});
												   //d2.push({}); 
                                            	});
												tableau_brut.getView().mydata = d2; // on ne passe pas par un store car cela prends trop de temps CPU
												/*
												var d2=[]; // nous créons quand même 
												for (var i=0;i<d.length;i++) {
													d2.push({}); 
												}
												*/
												//console.log("d2",d2);
												var store = Ext.create('Ext.data.Store',{	
                                            			//fields:fields,												
                                            			//model:'ColModelR',
														fields:['aucun'],
														data:d,
														proxy: {
														  type: 'memory',
														  reader: {
															type: 'json'
															
														  }
														}	
                                                    }); 
                                                
												
												//var columns=[new Ext.grid.RowNumberer({resizable: true,autoSizeColumn : true})];
												var columns=[{text:"",renderer:renderer}]; 
												// peut etre qu'il ne faut pas recréer le columns car on perd sinon le fait l'association avec les cloneNode
												indexCol=1;
												for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {													
													columns.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
												}			
												
												Ext.suspendLayouts();	
												//console.log("columns,",columns);
												//console.log("before reconfigure");	
												//console.log("columns=",columns);
												tableau_brut.reconfigure(store,columns);
												Ext.resumeLayouts(true);  
											},'xml');
						},
						
						listeners: {
							beforerender:function() {
									
									var me=this;
									var combo = me.up("window").down("#choix_filtre");
									combo.store.on('load',function(store, records, successful, eOpts) {									
										  store.insert(0, [{
											  name: 'Aucun filtrage',
											  id: -1
										  }]);					
										  
										if (store.indexOfId( me.current_filtreId ) == -1) { // the current_filterId is not in the list (has been deleted)
											me.current_filtreId = -1; // reinit it
											me.up("window").tools["refresh"].show();
											combo.suspendEvents();
											combo.setValue(me.current_filtreId);
											combo.resumeEvents(false);
										} else {
										 combo.setValue(me.current_filtreId);
										}
									 });
									 
									combo.on("change",function(combo, newValue, oldValue, eOpts) {									
										if (newValue!=me.current_filtreId) {
											me.current_filtreId = newValue;	
											me.loadTableauBrut(true);
										}
									});
								},
							afterrender:function(c) {
									
								if (theapp.user.credential.readwrite==false) {
									var saveButton = this.up("window").down("#saveButton");
									saveButton.setDisabled(true);
								} 
									var view = c.getView();
									
									/* gestion dd pour modif de modalite */
									var dd = view.findPlugin('gridviewdragdrop'); // http://docs.sencha.com/extjs/5.1/5.1.0-apidocs/#!/api/Ext.tree.ViewDropZone-method-onNodeEnter																		
									dd.dragZone.getDragData=function() {
										/*
										console.log("getdragData=",view.grid.view);
										var recs = [];
										// todo : utiliser data peut etre
										$.each(view.current_,function(i,v) { // laurent
											
											if (v.id.indexOf("SimpleModaliteModel")==0) {
												recs.push(v);
											}
										});
										*/
										
										return {records:view.current_selection_items};
										
									};
									dd.dragZone.onInitDrag= function(x, y) {
										if (view.current_selection_items.length == 0) { 
											return false;
										}
										
										var me = this;
											

										me.ddel.setHtml(me.getDragText());
										me.proxy.update(me.ddel.dom);
										me.onStartDrag(x, y);
										return true;
									};
									
									dd.dragZone.onBeforeDrag = function(data, e ) {
										
										if (view.current_selection_items.length == 0) { 
											return false;
										}
										
										/*
										var columns=[{"index":""}];
										for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {													
											flattenColumn(columns,tableau_brut.clioxml_columns[i].cloneNode,tableau_brut.clioxml_columns[i].originalNode);
										}	
												
										
										
										c.current_path = columns[e.position.colIdx].path;
										c.current_path_type = columns[e.position.colIdx].type;
										
										console.log("XXXc.current_path is",c.current_path);
										console.log("XXXc.current_path_type is",c.current_path_type);
										*/
										return true;
										
									};
									
									
									
									
									//var tf = Ext.ComponentQuery.query("Modalites #newCol")[0];
									/* ajout du menu */
									var menu = c.headerCt.getMenu();
									menu.removeAll();
									var menuExpand = menu.add({
										text: 'expand',
										handler: function(menuitem, e, opt) {
											var col = menuitem.ownerCt.activeHeader.getcol();
											console.log("col=",col);
											c.expandColumn(menuitem.ownerCt.activeHeader.getcol());
											c.loadTableauBrut(false);
										}
									});
									var menuRemoveCol = menu.add({
										text: 'enlever',
										handler: function(menuitem, e, opt) {											
											c.removeColumn(menuitem.ownerCt.activeHeader.getcol());
											c.loadTableauBrut(true);
										}
									});
									
									var menuCollapse = menu.add({
										text: 'collapse',
										handler: function(menuitem, e, opt) {
											c.collapseColumn(menuitem.ownerCt.activeHeader.getcol());
											c.loadTableauBrut(false);
										}
									});
									
									menu.on('beforeshow', function() {
										//console.log( menu.activeHeader);
									   //var currentDataIndex = menu.activeHeader.dataIndex; 
									   var currentCol = menu.activeHeader.getcol(); // config.getcol()
									   
									   if (!currentCol.data.leaf) {
										menuRemoveCol.show();
									   }
									   
										if (!currentCol.data.leaf && !currentCol.data.expanded) {
											menuExpand.show();
										} else {
											menuExpand.hide();											
										}
										
										if (!currentCol.data.leaf && currentCol.data.expanded) {
											menuCollapse.show();
										} else {
											menuCollapse.hide();
										}
										
										
										/*
										 style:
						                    {
						                        backgroundColor: 'red',
						                        color: 'white'
						                    },
										 */
									});
									/* fin ajout du menu */
									
									var tf = this.down('#newCol');
									var tableau_brut = this;
									var dd = new Ext.dd.DropTarget(tf.getEl(), {
										// must be same as for tree
										 ddGroup:'t2div'
										,notifyDrop:function(dd, e, node) {																					
											//console.log("dropped node : ",node.records[0]);						
											var els = schemaNode_to_array(node);
											//var child = {nb:0,appendChild:function() {nb++}};
											var current_node = node.records[0].clone(); // attention: c'est un clone donc son nom complet ne prend pas en compte les anciens parents parents
											current_node.data.expanded = false; // obligatoire : car si dans le schema le noeud était ouvert cela rentre en conflit avec l'expand de la colonne
											
											// is the current node has child ?
											
											
											
											/*
											var expandable = !current_node.data.leaf;
											// auto expand si le noeud le permet
											if (expandable) {			
												tableau_brut.expandColumn(current_node);
												
												var firstChild = current_node.childNodes[0];
												if (!firstChild.data.leaf) {
													tableau_brut.expandColumn(firstChild);
												}
												
											}
											*/
											
											tableau_brut.clioxml_columns.push({originalNode:node.records[0],cloneNode:current_node});
											
											tableau_brut.loadTableauBrut(true);
											
											
											
											
											
											return true;
										} // eo function notifyDrop
									});
								}
							}
                    } // grid du tableau brut
					] // items de window
				
            });
        
        return win;
    }
});


var renderer = function(val, meta, rec, rowIdx, colIdx, store, view) {	
	
	
	var result=[];
	var r = view.mydata[rowIdx];
	if (colIdx==0) {
		return r.rownum;
	} 
	var colvalue = r.cols[colIdx];
	var clioxml_modify = colvalue.clioxml_modify;
	var clioxml_original_value = colvalue.clioxml_original_value;
	var modify ="";
	if (clioxml_modify!=null && clioxml_modify!="") {
		//modify = "["+clioxml_modify+"]("+clioxml_original_value+")";
		modify = '<img onclick="alert(\'toto\')" src="resources/images/icon_c.jpg"/>';
	}
	var val = colvalue.value;
	if (val!=null) 
		return val+modify;
	else	
		return "";
		
	
	//console.log("xx",r._getCols());
	var col = r.cols[colIdx];
	
	var subcols = col.subcols;
	
	var result=[];
	for (var i=0;i<subcols.length;i++) {
    	var subcol = subcols[i];    
    	
    	var clioxml_modify = subcol.clioxml_modify;
    	var clioxml_original_value = subcol.clioxml_original_value;
    	var modify ="";
    	if (clioxml_modify!=null && clioxml_modify!="") {
    		//modify = "["+clioxml_modify+"]("+clioxml_original_value+")";
			modify = '<img onclick="alert(\'toto\')" src="resources/images/icon_c.jpg"/>';
    	}
    	var val = subcol.value;
    	if (val!=null) 
    		result.push(val+modify);
    	else	
    	    result.push("");
	}
	return result.join('<br/>');
	
	/*
	var clioxml_modify = r.get("c_modify"+(colIdx));
	var clioxml_original_value = r.get("clioxml_original_value"+(colIdx));
	var modify ="";
	if (clioxml_modify!=null) {
		modify = "/"+clioxml_modify+"/"+clioxml_original_value;
	}
	var val = r.get("c"+(colIdx));
	if (val!=null) 
		return val+modify;
	else return "";
	*/
};

var indexCol=1; // TODO mettre cela dans le grid !


function addPathOld(paths,col) {
	
	
	if (col.data.expanded == true) {
		var subcolumns = [];
		for (var i=0;i<col.childNodes.length;i++) {
			addPath(paths,col.childNodes[i])			
		}		
	} else {		
		var name = col.data.name;
		if (col.data.schemaNode.localName == 'attribute') {
			name = "@"+name;
		}
		paths.push(name); 		
	}
	
	
	
}

function addPath(parentName,col) {
	var name = col.data.name;
	if (col.data.expanded == true) {
		var subcolumns = [];
		for (var i=0;i<col.childNodes.length;i++) {
			var n = parentName+"/"+name;
			if (parentName == '') {
				n = "";
			}
			subcolumns.push( addPath(n,col.childNodes[i]));
			
		}		
		return subcolumns;
	} else {		
		
		if (col.data.schemaNode.localName == 'attribute') {
			//name = "@"+name;
			name = name;
		}
		if (parentName == '') 
			return name;
		 else 
			return parentName+"/"+name; 		
	}
	
	
	
}
/*
		hasMany: [
			  { fields:[
				{name:'c',mapping:'c'},
				{name:'c_modify',mapping:'c @clioxml_modify'},
				name: 'c', 
				associationKey:'c' 
				hasMany: [sc]
				], 
				}
			]
	*/		
	// deprecated ?
function addField(fields,col) {
	var c = {fields:""};
	
	if (col.data.expanded == true) {
		var subcolumns = [];
		for (var i=0;i<col.childNodes.length;i++) {
			addField(fields,col.childNodes[i])			
		}		
	} else {		
		fields.push({name:'c'+indexCol,mapping:'c:nth('+(indexCol+1)+')'}); // voir Ext.dom.Query pour les sélecteurs dispo // TODO ajouter le type + tard (donné par le schéma node)
		fields.push({name:'c_modify'+indexCol,mapping:'c:nth('+(indexCol+1)+') @clioxml_modify'}); 
		fields.push({name:'clioxml_original_value'+indexCol,mapping:'c:nth('+(indexCol+1)+') @clioxml_original_value'}); 
		indexCol += 1;
	}
	
	
	return c;
}


function flattenColumn(columns,col,originalCol) {
	var prefix = schemaNode_to_array2(originalCol);
	var suffix = schemaNode_to_array2(col);
	suffix.shift();
	prefix = prefix.concat(suffix);
	
	var c = {flex:1,type:col.data.type,name:col.data.name,path:getFullPathNS_from_array(prefix)}; 
	
	
	if (col.data.expanded == true) {
		
		for (var i=0;i<col.childNodes.length;i++) {
			flattenColumn(columns,col.childNodes[i],originalCol);
			
		}
		
	} else {
		indexCol += 1;
		//console.log("prefix is ",prefix);
		columns.push(c);
	}
	
	
	
}


function getColumn(col) {
	var c = {text:col.data.name,getcol : function() {return col;}}; //, flex:1,autoSizeColumn : true componentCls:'leafcol-style', overCls:'leafcol-style ',baseCls: 'leafcol-style' cls:'leafcol-style',baseCls:'leafcol-style',overCls:'leafcol-style x-column-header-over'
	if (col.data.leaf) {
		c.componentCls = "leafcol-style";
	}
	if (col.data.expanded == true) {
		var subcolumns = [];
		for (var i=0;i<col.childNodes.length;i++) {
			var sc = getColumn(col.childNodes[i]);
			subcolumns.push(sc);
		}
		c.columns = subcolumns;
	} else {
		c.renderer=renderer;
		//c.dataIndex="c"+indexCol;
		
		indexCol += 1;
	}
	
	
	return c;
}
