Ext.define('ModaliteInitiale',{
	extend: 'Ext.data.Model',
	fields: ['old_modalite','old_count'],
	
	proxy: {
      //type: 'memory',
      reader: {
        type: 'xml',
        root: 'clioxml_initial_value',
        record: 'c'
      }
    }
	
 });
 
    Ext.define('ModaliteModel',{ // was book
        extend: 'Ext.data.Model',
		requires:[
			'ModaliteInitiale'         
		],
        fields: [
            // set up the fields mapping into the xml doc
            // The first needs mapping, the others are very basic           
            'modalite', {name:'count',type:"integer"},'clioxml_modify'
			],
			hasMany: [
			  { model: 'ModaliteInitiale', name: 'clioxml_initial_value', associationKey:'clioxml_initial_value' }
			]
			
        
    });

	Ext.define('ModalitesStore', {	
		extend: 'Ext.data.Store',
        model: 'ModaliteModel',
        //autoLoad: true,
        proxy: {
            // load using HTTP
            type: 'ajax',
            
			actionMethods: {
                            create: 'POST',
                            read: 'POST',
                            update: 'POST',
                            destroy: 'POST'
                        },
            reader: {
                type: 'xml',               
                record: 'r'                
            }
        }
    })

function hasParentInactive(record) {
		
		var current_node = record.parentNode;
		while (current_node.parentNode!=null) {			
			
			if (!current_node.get('active')) {
				return true;
			}
			current_node = current_node.parentNode;
		}
		return false;
	}	

Ext.define('Desktop.ModalitesWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'modalites-win',

    init : function(){
        this.launcher = {
            text: 'Modalites Window',
            iconCls:'icon-grid'
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Modalités',
				y:0,
				x:400,
                width:740,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'border',
						
				
					
					items: [
                    {
                        border: false,
                        xtype: 'grid',
						store : Ext.create('ModalitesStore'),
						selModel: {
							mode: 'MULTI',
							allowDeselect: true
						},
						current_path:null,
						columns: [
							{
								text: "modalite", flex: 1, dataIndex: 'modalite', sortable: true,
								
								renderer:function(value, d) {			
									var icon="";
									
									if (d.record.get('clioxml_modify')!=null && d.record.get('clioxml_modify')!='') {
										icon = '<img onclick="alert(\'titi\')" src="resources/images/icon_c.jpg"/>';
									}
									
									return value+icon;
								}
							},
							{text: "ancienne(s) valeur(s)", flex: 1, 
								renderer:function(value, d) {									
									var ivalues = d.record.clioxml_initial_value();
									var r=[];
									for (var i=0;i<ivalues.getCount();i++) {
										var obj = ivalues.getAt(i);											
										r.push(obj.get('old_modalite')+" ("+obj.get('old_count')+")");
									}
									
									return r.join(" / ");
									
								}
							},
							{text: "recodage", flex: 1, dataIndex: 'clioxml_modify', sortable: true},
							{text: "count", width: 180, dataIndex: 'count', sortable: true}
						],
						listeners: {
							afterrender:function() {
								//var tf = Ext.ComponentQuery.query("Modalites #newCol")[0];		
								var tf = this.up("window").down('#newCol');
								var thegrid = this.up("window").down('grid');
								var therecodage = this.up("window").down('treepanel');
								var dd = new Ext.dd.DropTarget(tf.getEl(), {
									// must be same as for tree
									 ddGroup:'t2div'

									
									
									,notifyDrop:function(dd, e, node) {
									
										
										var els = schemaNode_to_array(node);
										
										tf.setValue(getFullPath_from_array(els));
										
										
										var m = thegrid;
										m.current_path = getFullPathNS_from_array(els) ; //getFullPath_from_array(els);
										var store = m.getStore();
										
										var url="/service/commands?cmd=executeRawXQuery";
										
										store.getProxy().setUrl(url);
										store.getProxy().extraParams ={xquery:getXQuery_modalite(getFullPathNS_from_array(els))};
										store.load();
										
										 
										//recodage_store.load();
										var recodage_store = therecodage.getStore();
										recodage_store.getProxy().extraParams.fullpath = getFullPathNS_from_array(els);
										recodage_store.load();
										
										
										
										return true;
									} // eo function notifyDrop
								});
							}
						},
						flex:1,
						region:'center',
						viewConfig: {
								//enableLocking : true,
								copy:true,
								plugins: {
									ptype: 'gridviewdragdrop',
									dragGroup: 'modaliteDrop'
								}
							}
						
                      },
					{
						region:'south',
						xtype:'treepanel',
						title:'Recodage',
						split:true,
						collapsible: true,
						height:'300',
						layout:'fit',
						store:Ext.create('Ext.data.TreeStore',{
											autoLoad:false,
											proxy: {
												type: 'ajax',
												//url: 'check-nodes.json',
												url:'/service/commands',
												extraParams: {
													cmd:'getListModifyModalite'													
												} //  reader : idProperty: 'sitecode'
											}
										}),
						
						rootVisible: false,
						useArrows: true,
						frame: false,
						// hideHeaders: true,
						disableSelection: true,
						viewConfig: {			
							getRowClass: function( record, index ) {
								if (hasParentInactive(record) || record.get('active') == false) {
									return 'rowinactive'; 
								} else {
									return '';
								}
							}		,
							plugins: {
											ptype: 'treeviewdragdrop',
											dropGroup: 'modaliteDrop',
											enableDrag:false
											
											//dragText: 'Drag and drop to add to workflow'
										}		
										
						},
						selType: 'rowmodel',
						plugins: [
							Ext.create('Ext.grid.plugin.CellEditing', {
								clicksToEdit: 1
							})
						],			
						
							
						
						columns:[
							{
								xtype: 'treecolumn',
								text:'',
								width:100,
								dataIndex:'text',
								autoSizeColumn: true ,
								renderer : function (value, metadata, record, rowIndex, colIndex, store){	
									if (record.get('leaf') == true) {
										metadata.tdAttr  = 'data-qtip="' + value + ' occurences"';						
									}
									return value;
								}
								
								
							},
							{
								xtype:'checkcolumn', dataIndex:'active', width:30,
								
								listeners: {
									beforecheckchange:function(checkbox, rowIndex, checked, eOpts ) {
										var rec = checkbox.up('panel').getStore().getAt(rowIndex);
										
										if (hasParentInactive(rec)) {
											return false; // false = interdire le changement
										}
										return true; 
									},
									checkchange  : function (checkbox, rowIndex, checked, eOpts) {
										
										checkbox.up('panel').getView().refresh();
									}
								}
							},
							{
								dataIndex:'modalite',flex:1,
								text:'modalite',
								renderer : function (value, metadata, record, rowIndex, colIndex, store){	
									if (record.get('leaf') == true) {
										metadata.tdAttr  = 'data-qtip="ancienne modalité"';						
									} else if (record.get("text")!="chemin") {
										metadata.tdAttr  = 'data-qtip="nouvelle modalité"';	
									}
									if (value=='') {
										return '(vide)';
									}
									return value;
								},
								getEditor: function(record){	
										if (record.get('leaf') == false && record.get("text")!="chemin") {
											var mods=[record.get('modalite')];
											
											var newValue = record.get('newValue'); // on ajoute la valeur initiale (sans la perdre car dans le champs newValue)
											if (mods.indexOf(newValue)<0) {
												mods.push(newValue);
											}
											Ext.each(record.get('children'),function(v) {	
												if (mods.indexOf(v.modalite)<0) {
													mods.push(v.modalite);
												}
											});
											return Ext.create('Ext.grid.CellEditor', {								
												field: {
													xtype:"combo",
													emptyText:'(vide)',
													value:record.get("modalite"),
													store:mods,
													enableKeyEvents: true,
													listeners: {
														specialkey: function (field, e) {
																if (e.getKey() == e.ENTER) {
																	field.setValue(field.getRawValue());
																}
															}
														} // listeners
													}
											});
										}
										
									}
							}
							/*
							,{
								xtype:'actioncolumn',width:20,
								 items: [{
									iconCls: 'delete-icon',
									// Use a URL in the icon config
									tooltip: 'Supprimer',
									handler: function (grid, rowIndex, colIndex) {
										var rec = grid.getStore().getAt(rowIndex);
										alert("remove " + rec.get('modalite'));
									}
								}]
							}*/
							] ,// fin columns,
							tbar:[
								{
									xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'nouveau codage',itemId:"newCodage",editable:false,emptyText:'coller une ou plusieurs modalité(s)'
								}
							],
							bbar:[
								{
									text:"Appliquer les modifications",
									listeners:{										
										'click':function(button) {
											var codage_panel = this.up('treepanel');
											var modalites_panel = this.up('window').down('grid');
											var store = codage_panel.getStore();
											
											if (store.getModifiedRecords().length == 0) {
												return;
											}
											
											
											var modifications=[];
											var records = store.getModifiedRecords();
											for (var i=0;i<records.length;i++) {
												var r = records[i];
												if (r.isLeaf()) {
													// take the parent
													r = r.parentNode;										
												}
												
												if (!valInTab(modifications,'extjs_id',r.get('id'))) {
													var old_values=[];
													
													for( var j=0;j<r.childNodes.length;j++) {
														var c = r.childNodes[j];
														old_values.push({old_value:c.get('modalite'),active:c.get('active')});
													}												
													var pmid = r.get('pmid');
													if (r.get("text") == 'nouveau') {
														pmid=0;													
													}
													modifications.push({extjs_id:r.get('id'),order:r.get('order_modify'),type:r.get('type'),path:modalites_panel.current_path,id:pmid,active:r.get('active'),new_value:r.get('modalite'),old_values:old_values});
												}
											}
											
											//remove of extjs_id
											for (var i=0;i<modifications.length;i++) {
												delete modifications[i].extjs_id;
											}
											codage_panel.setLoading(true);
											$.post("/service/commands",{cmd:'updateMods',path:modalites_panel.current_path,codages:Ext.encode(modifications)},function() {
												codage_panel.setLoading(false);
												store.removeAll(); // sinon pas de reinit de modifiedRecords !
												store.load();
												modalites_panel.getStore().load();
											});
											
											
											
										}
									}
								}							
							],
							listeners: {
								viewready: function (tree) {
									
									var view = tree.getView(),
										dd = view.findPlugin('treeviewdragdrop'); // http://docs.sencha.com/extjs/5.1/5.1.0-apidocs/#!/api/Ext.tree.ViewDropZone-method-onNodeEnter
									
									
									dd.dropZone.onNodeOver = function ( nodeData, source, e, data ) {
												
										var rec = view.getRecord(e.getTarget(view.itemSelector));										
										
										if (rec.isLeaf()) 
											return  this.dropNotAllowed ;
										else
											return this.dropAllowed;
									};
									
									dd.dropZone.onNodeDrop = function ( nodeData, source, e, node ) {
																				
										
										var rec = view.getRecord(e.getTarget(view.itemSelector));	
										
										
										for(var i=0;i<node.records.length;i++) {
											rec.appendChild({modalite:node.records[i].get('modalite'),leaf:true,iconCls:'noicon',text:'--',active:true}); // getAt(i) sinon												
										}
											
										
										
										return true;
									};
									
									
									
								},
								afterrender:function() {
									//var tf = Ext.ComponentQuery.query("Modalites #newCol")[0];		
									var tf = this.down('#newCodage');
									var recodage_grid = this;
									var dd = new Ext.dd.DropTarget(tf.getEl(), {
										// must be same as for tree
										 ddGroup:'modaliteDrop'

										
										
										,notifyDrop:function(dd, e, node) {
										
											var store = recodage_grid.getStore();
											var children=[];
											for(var i=0;i<node.records.length;i++) {
												children.push({modalite:node.records[i].get('modalite'),leaf:true,iconCls:'noicon',text:'--',active:true}); // getAt(i) sinon												
											}
											store.getRootNode().appendChild({text:'nouveau',modalite:'',newValue:'',expanded:true,iconCls:'noicon',active:true,children:children});
											recodage_grid.getView().refresh();
											return true;
										} // eo function notifyDrop
									});
								}
							}
					} // fin xtreepanel
					  
                ],
                tbar: [
						{
							xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'liste des modalités pour le noeud',itemId:"newCol",editable:false,emptyText:'coller un noeud xml'}
						,{
							text:"export",
							listeners:{
								
								'click':function(button) {
									var f = $("#formDownload");				
									f.empty();
									
									f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadModalites"));			
									f.append($("<input>").attr("type", "hidden").attr("name", "path").val(button.up('window').down("grid").current_path));
									f.submit();					
								}
							}
						}
						]
            });
        
        return win;
    }
});

function valInTab(tab,attr,value) {
	// return  true if exists an object with the attr equals to value
	for( var i = 0; i<tab.length;i++) {
		var obj = tab[i];
		if (attr in obj) {
			if (obj[attr] == value) {
				return true;
			}
		}
	}
	
	return false;
}

function schemaNode_to_array2(extjs_node) {
	var els=[];
	var currentNode = extjs_node;
	while (currentNode!=null) {												
		if (currentNode.data.schemaNode.localName=='attribute') {
			els.push({name:"@"+currentNode.data.name,name_ns:"@"+currentNode.data.name});			
		} else {
			els.push({name:currentNode.data.name,name_ns:"Q{"+currentNode.data.ns+"}"+currentNode.data.name});			
		}						
		currentNode = currentNode.parentNode;
	};
	els.reverse();
	return els;
}

function schemaNode_to_array(node) {
	var els=[];
	var currentNode = node.records[0];
	while (currentNode!=null) {												
		if (currentNode.data.schemaNode.localName=='attribute') {
			els.push({name:"@"+currentNode.data.name,name_ns:"@"+currentNode.data.name});			
		} else {
			els.push({name:currentNode.data.name,name_ns:"Q{"+currentNode.data.ns+"}"+currentNode.data.name});			
		}						
		currentNode = currentNode.parentNode;
	};
	els.reverse();
	return els;
}

function getFullPathNS_from_array(els) {
	var p="";
	for(var i=0;i<els.length;i++) {
		p+="/"+els[i].name_ns;
	}
	return p;
}

function getFullPath_from_array(els) {
	var p="";
	for(var i=0;i<els.length;i++) {
		p+="/"+els[i].name;
	}
	return p;
}

function getXQuery_modalite(fullpath_withns) {
	var xquery="for $d in $last_collection"+fullpath_withns+" \n";
	xquery +="let $elem:= $d \n";
	xquery +="let $atLeastOneMod:= tokenize($elem/@clioxml_modify,'\\s') \n";
	xquery +="let $original_value:= data($elem/@clioxml_original_value) \n";
	xquery +="group by $elem  \n";
	
	xquery +="let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c>  \n";
	xquery +="return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>";
					
	return xquery;
}