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
            text: 'Tableau Brut Window',
            iconCls:'icon-grid'
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
		var theapp = this.app;
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Tableau Brut',
                width:$(window).width()*0.4,
				y:0,
				x:$(window).width()*0.3,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
				app:theapp,
				listeners:{
					afterrender:function() {
						var me = this;
						this.addTool({
							  type:'refresh',
							  hidden:true,
							  handler: function() {
								var tab = me.down("grid");
								tab.loadTableauBrut();
								
								me.tools["refresh"].hide();
							  },
							  scope:me
							});
							
						
						
						this.app.addListener({'codageUpdated':function() {me.tools["refresh"].show();}});
					}
					
				},
                items: [
                    {
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
										console.log("selModel select");
										return false;
										console.log("selModel select");
										console.log(row,columnIndex);
										var r = selModel.view.mydata[row];
										
										var col = r.cols[columnIndex];
										console.log("col=",col);
										
										record.set('modalite',col.subcols[0].value); 
									
								}
								
								
							}
						},
						
						
						viewConfig: {
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
										if (col.subcols.length>1) {
											return; // not a final col
										}										
										console.log("col is",col);
										var value = col.subcols[0].value;
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
						tbar: [
							{xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'Ajouter une colonne',itemId:"newCol",editable:false,emptyText:'coller un noeud xml'}
							,{
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
									f.append($("<input>").attr("type", "hidden").attr("name", "filtre").val(JSON.stringify([])));
									f.submit();					
								}
							}
						}
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
							current_node.data.expanded = true;
							
							if (current_node.childNodes.length>0) {
								return; // le noeud a déjà été 'parsé'
							}
							var elementStructure = getSchemaElementStructure(current_node.data.schemaNode);						
						
							if (elementStructure.localName =='complexType' ) {
								// start with attributes
								$.each(elementStructure.childNodes,function(key,val) {
									if (val.localName == 'attribute') {										
										var n = {
											"iconCls":"task",
											"name":get_ns_element_name(val).name,
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
						loadTableauBrut:function() {
							var tableau_brut = this;
							this.getView().removeSelection();
							tableau_brut.setLoading(true);
											var cols=[];
											var subcols=[];	
											indexCol=1;											
											for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {
												cols.push(getFullPathNS_from_array(schemaNode_to_array2(tableau_brut.clioxml_columns[i].originalNode))); 
												subcols.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
											}
											
																				
											
											
												
											$.post('/service/commands?cmd=getTableauBrut',{colonnes:JSON.stringify(cols),start:1,subcols:JSON.stringify(subcols),filtre:JSON.stringify([])},function(data, textStatus, jqXHR ) { 
												tableau_brut.setLoading(false);
												tableau_brut.up("window").tools["refresh"].hide();
												
												var d=[];
												var d2=[];  // nous créons quand même un 'faux' tableau contenant seulement le nombres de lignes nécessaires
												$.each($(data).find("rows").children(),function(i,arow) {                                            	   
												   var cols=[];
                                            	   $.each($(arow).children(),function (j,acol) {     
														var subcols=[];                                                	    
                                                	    $.each($(acol).children(),function (k,asubcol) {            	                                                        	        
																//subcols.push({value:$(asubcol).text(),clioxml_modify:$(asubcol).attr("clioxml_modify"),clioxml_original_value:$(asubcol).attr("clioxml_original_value")});
																subcols.push({value:$(asubcol).text(),clioxml_modify:$(asubcol).attr("clioxml:node__pmids"),clioxml_original_value:$(asubcol).attr("clioxml:node__oldvalue")});
                                                	    });
														cols.push({subcols:subcols});
                                            	   });
												   
												   d.push({cols:cols});
												   d2.push({}); 
                                            	});
												tableau_brut.getView().mydata = d; // on ne passe pas par un store car cela prends trop de temps CPU
												/*
												var d2=[]; // nous créons quand même 
												for (var i=0;i<d.length;i++) {
													d2.push({}); 
												}
												*/
												console.log("d2",d2);
												var store = Ext.create('Ext.data.Store',{	
                                            			//fields:fields,												
                                            			//model:'ColModelR',
														fields:['aucun'],
														data:d2,
														proxy: {
														  type: 'memory',
														  reader: {
															type: 'json'
															
														  }
														}	
                                                    }); 
                                                
												
												var columns=[new Ext.grid.RowNumberer({resizable: true,autoSizeColumn : true})];
												// peut etre qu'il ne faut pas recréer le columns car on perd sinon le fait l'association avec les cloneNode
												indexCol=1;
												for (var i=0;i<tableau_brut.clioxml_columns.length;i++) {													
													columns.push(getColumn(tableau_brut.clioxml_columns[i].cloneNode));
												}			
												
												Ext.suspendLayouts();	
												//console.log("columns,",columns);
												//console.log("before reconfigure");	
												console.log("columns=",columns);
												tableau_brut.reconfigure(store,columns);
												Ext.resumeLayouts(true);  
											},'xml');
						},
						
						listeners: {
							afterrender:function(c) {
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
										var me = this;
											

										me.ddel.setHtml(me.getDragText());
										me.proxy.update(me.ddel.dom);
										me.onStartDrag(x, y);
										return true;
									};
									
									dd.dragZone.onBeforeDrag = function(data, e ) {
										//return false;
										//console.log("onBeforeDrag data is ",data);
										
										//console.log("selectionModel",c.getSelectionModel());
										//console.log("selectionModel.",c.getSelectionModel()); // getSelection
										c.current_path = getFullPathNS_from_array(schemaNode_to_array2(c.clioxml_columns[e.position.colIdx-1].originalNode));
										
										
										//console.log("beforeDrag data=",data);
										return true;
										/*
										// on ne peut pas utiliser data.Records car mes data sont custom (sans modele, et sans passer la le systeme de store)
										// il faut récupérer la position (rowIdx et colIdx) de ts les élements dragger, a voir dans le e (.position ?)
										for(var i=0;i<data.records.length;i++) {
											console.log("data",i,data.records[i]);
											data.records[i].set('modalite','XX');
										}
										*/
									};
									
									
									
									
									//var tf = Ext.ComponentQuery.query("Modalites #newCol")[0];
									/* ajout du menu */
									var menu = c.headerCt.getMenu();
									menu.removeAll();
									var menuExpand = menu.add({
										text: 'expand',
										handler: function(menuitem, e, opt) {											
											c.expandColumn(menuitem.ownerCt.activeHeader.getcol());
											c.loadTableauBrut();
										}
									});
									var menuRemoveCol = menu.add({
										text: 'enlever',
										handler: function(menuitem, e, opt) {											
											c.removeColumn(menuitem.ownerCt.activeHeader.getcol());
											c.loadTableauBrut();
										}
									});
									
									var menuCollapse = menu.add({
										text: 'collapse',
										handler: function(menuitem, e, opt) {
											c.collapseColumn(menuitem.ownerCt.activeHeader.getcol());
											c.loadTableauBrut();
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
										
										
										
									});
									/* fin ajout du menu */
									
									var tf = this.down('#newCol');
									var tableau_brut = this;
									var dd = new Ext.dd.DropTarget(tf.getEl(), {
										// must be same as for tree
										 ddGroup:'t2div'
										,notifyDrop:function(dd, e, node) {																					
																					
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
											
											tableau_brut.loadTableauBrut();
											
											
											
											
											
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

var rendererOld = function(val, meta, rec, rowIdx, colIdx, store, view) {	
	console.log(view.mydata.length);
	
	var r = store.getAt(rowIdx);
	//console.log("xx",r._getCols());
	var col = r.getCols().getAt(colIdx);
	
	var subcols = col.getSubCols();
	
	var result=[];
	for (var i=0;i<subcols.getCount();i++) {
    	var subcol = subcols.getAt(i);    
    	
    	var clioxml_modify = subcol.get("clioxml_modify");
    	var clioxml_original_value = subcol.get("clioxml_original_value");
    	var modify ="";
    	if (clioxml_modify!=null && clioxml_modify!="") {
    		modify = "["+clioxml_modify+"]("+clioxml_original_value+")";
    	}
    	var val = subcol.get("value");
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

var renderer = function(val, meta, rec, rowIdx, colIdx, store, view) {	
	
	
	var r = view.mydata[rowIdx];
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
			name = "@"+name;
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


function getColumn(col) {
	var c = {text:col.data.name,getcol : function() {return col;}}; //, flex:1,autoSizeColumn : true
	
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
