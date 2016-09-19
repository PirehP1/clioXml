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


Ext.define('Desktop.ModalitesWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'modalites-win',

    init : function(){
        this.launcher = {
            text: 'Tris à plat (Modalité)',
            iconCls:'icon-grid',
            idmenu:"tri-simple"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var theapp = this.app;
           var win = desktop.createWindow({
                itemId: 'mod-win',
                title:'Modalités',
				y:0,
				x:$(window).width()*0.3,
                width:$(window).width()*0.4,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
				app:theapp,		
				
				execFromHisto(data) {
					var tf = this.down('#newCol');
					
					tf.setValue(removeQName(data.params.path));
					
					
					var grid = this.down("grid");
					grid.current_path = data.params.path ; //getFullPath_from_array(els);
					grid.current_path_type = data.params.path_type;
					
					grid.current_filtreId = data.params.filtreId;// reinit it
					
					var combo = this.down("#choix_filtre");
					combo.suspendEvents();
					combo.setValue(grid.current_filtreId);
					combo.resumeEvents(true); // true = discardQueuedEvents
					
					grid.load_data(false);
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
										icon = '<img onclick="displayCodage(event,'+d.record.get('clioxml_modify')+')" src="resources/images/icon_c.jpg"/>';
									}
									
									return value+icon;
								}
							},
							/*
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
							*/
							{text: "recodage", flex: 1, dataIndex: 'clioxml_modify', sortable: true},
							{text: "count", width: 180, dataIndex: 'count', sortable: true}
						],
						listeners: {
							beforerender:function() {
								
								var me=this;
								var combo = me.up("window").down("#choix_filtre");
								combo.store.on('load',function(store, records, successful, eOpts) {	
									combo.suspendEvents();
									
									  store.insert(0, [{
										  name: 'Aucun filtrage',
										  id: -1
									  }]);
									if (store.indexOfId( me.current_filtreId ) == -1) { // the current_filterId is not in the list (has been deleted)
										me.current_filtreId = -1; // reinit it
										me.up("window").tools["refresh"].show();
										
										//combo.suspendEvents();
										combo.setValue(me.current_filtreId);
										//combo.resumeEvents(false);
									} else {										
									  combo.setValue(me.current_filtreId);
									}
									
									combo.resumeEvents(true);
								 });
								 
								
								combo.on("change",function(combo, newValue, oldValue, eOpts) {									
									me.current_filtreId = newValue;
									
									me.load_data(true);
								});
								
								
								
							},
							afterrender:function() {
								
								
								var me=this;
								
								
									me.up("window").addTool({
									  type:'refresh',
									  hidden:true,
									  handler: function() {
										me.up("window").down("grid").getStore().load();
										
										//removeTool(me.up("window"),"refresh");
										//me.up("window").tools["refresh"].remove();
										
										me.up("window").tools["refresh"].hide();
									  },
									  scope:me
									});
									
								
								var showRefresh = function() {me.up("window").tools["refresh"].show();};
								var showRefreshFiltre = function(source,modifiedFiltreId) {
									var combo = me.up("window").down("#choix_filtre");
									
									combo.getStore().reload();
									var current_filtreId = combo.getValue();
									if (current_filtreId!=-1 && current_filtreId == modifiedFiltreId) {
										me.up("window").tools["refresh"].show();
									}									
								};
								
								this.up("window").addListener({'beforeclose':function() {
									me.up("window").app.removeListener("codageUpdated",showRefresh); 
									me.up("window").app.removeListener("filtreUpdated",showRefreshFiltre); 
								}});
								this.up("window").app.addListener({'codageUpdated':showRefresh});
								this.up("window").app.addListener({'filtreUpdated':showRefreshFiltre});
								
								//var tf = Ext.ComponentQuery.query("Modalites #newCol")[0];		
								var tf = this.up("window").down('#newCol');
								var thegrid = this.up("window").down('grid');
								//var therecodage = this.up("window").down('treepanel');
								var dd = new Ext.dd.DropTarget(tf.getEl(), {
									// must be same as for tree
									 ddGroup:'t2div'

									
									
									,notifyDrop:function(dd, e, node) {
										if (!node.records[0].data.leaf) {
											Ext.Msg.alert('Attention', "Vous comptez l'agregat de noeuds");
											
											
											node.records[0].expand();
											return;
										}
										
										var els = schemaNode_to_array(node);
										
										tf.setValue(getFullPath_from_array(els));
										
										
										var m = thegrid;
										m.current_path = getFullPathNS_from_array(els) ; //getFullPath_from_array(els);
										m.current_path_type = node.records[0].data.type;
										//console.log("type is",m.current_path_type);
										
										m.load_data(true);
												
										return true;
									} // eo function notifyDrop
								});
								
								
							}
						},
						load_data:function(addToHisto) {
							
							var m = this;
							if (m.current_path == null) return;
							var store = m.getStore();
							/*
							var url="/service/commands?cmd=executeRawXQuery";
							
							store.getProxy().setUrl(url);
							store.getProxy().extraParams ={xquery:getXQuery_modalite(m.current_path),filtreId:m.current_filtreId};
							*/
							var url="/service/commands?cmd=getListModalites";
							
							store.getProxy().setUrl(url);
							store.getProxy().extraParams ={path:m.current_path,filtreId:m.current_filtreId};
							
							store.load();
							if (addToHisto) {
								var combo = this.up("window").down("#choix_filtre");
								
								var filtreName = "";
								if (m.current_filtreId!=-1) {
									filtreName = combo.getStore().findRecord('id',m.current_filtreId).data.name;
								}
								m.up("window").app.addToHisto({from:"modalite",type:"query",params:{path_type:m.current_path_type,path:m.current_path,filtreName:filtreName,filtreId:m.current_filtreId},timestamp:null}); //{from:"modalite",type:"query",params:{},timestamp:null}
							}
							m.up("window").tools["refresh"].hide();
						},
						viewConfig: {
								//enableLocking : true,
								copy:true,
								plugins: {
									ptype: 'gridviewdragdrop',
									dragGroup: 'modaliteDrop'
								}
							}
						
                      }
					  
                ],
               
                tbar: [
						{
							xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'modalités pour le noeud',itemId:"newCol",editable:false,emptyText:'coller un noeud xml'}
						,{
							text:"export",
							listeners:{
								
								'click':function(button) {
									var f = $("#formDownload");				
									f.empty();
									
									f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadModalites"));
									var current_path = button.up('window').down("grid").current_path;
									var filtreId = button.up('window').down("grid").current_filtreId;
									f.append($("<input>").attr("type", "hidden").attr("name", "path").val(current_path));
									f.append($("<input>").attr("type", "hidden").attr("name", "filtreId").val(filtreId));
									//var xq = getXQuery_modalite_for_download(current_path);									
									//f.append($("<input>").attr("type", "hidden").attr("name", "xquery").val(xq));			
									f.submit();					
								}
							}
						},
						{
							text:"xquery",
							listeners:{								
								'click':function(button) {
									// ouverture de l'éditeur xquery pour les modalites
									var url="/service/commands?cmd=getXQueryListModalites";
									var m = button.up('window').down("grid");
									$.post(url,{path:m.current_path,filtreId:m.current_filtreId,page:1,start:0,limit:25},function(response) {
										var module = new Desktop.XQueryEditorWindow();
										module.app = theapp;
										var xquery=response;		
										var editorWindow = module.createWindow(xquery);
										editorWindow.show();
									});
									
									
								}
							}
						}
						]
            });
        
        return win;
    }
});

function displayCodage(evt,num) {
	evt.stopPropagation();
	var menu_grid = new Ext.menu.Menu({
        items: 
        [
            {  xtype: 'menucheckitem',checked:true,text:'value1' },
			 {  xtype: 'menucheckitem',text:'value2' },
			 { xtype: 'menuseparator'},
            { text: 'Validez', handler: function() {console.log("Delete");} }
        ]
    });
	menu_grid.showAt([evt.clientX,evt.clientY]);
}

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
	//var xquery="let $last_collection := for $d in $last_collection where $d/Q{}prosopographie/Q{}personne/Q{}Sexe/text()='M' return $d\n";
	// on ne peut pas faire la requete précédente car prosopographie n'a qu'un seul fichier, donc nous avons toujours au moins un nom valide 
	// et cela va nous retourner tout le document ($d), ce qu'il faut c'est mettre le where après le for $d in $last_collection"+fullpath_withns
	// et le where doit être relatif à ce noeud
	var xquery="";
	xquery+="for $d in $last_collection"+fullpath_withns+" \n";
	//xquery +=" where $d/text()='M' \n";
	var attribute=null;
	if (fullpath_withns.indexOf("@")!=-1) {
		attribute = getLastNode(fullpath_withns).substring(1);
	}
	if (attribute==null) {
		xquery +="let $elem:= $d/text() \n";
		//xquery +="let $elem:= data($d/.) \n";
		xquery +="let $atLeastOneMod:= tokenize($elem/../@clioxml:node__pmids,'\\s') \n"; // .. car maintenant nous travaillons avec text()
		xquery +="let $original_value:= data($elem/../@clioxml:node__oldvalue) \n";
		
	} else {
		xquery +="let $elem:= $d \n";
		xquery +="let $atLeastOneMod:= tokenize($elem/../@clioxml:"+attribute+"__pmids,'\\s') \n";
		xquery +="let $original_value:= data($elem/../@clioxml:"+attribute+"__oldvalue) \n";
	}
	
	xquery +="group by $elem  \n";
	
	xquery +="let $ori := for $e in $original_value let $f:=$e group by $f return <c><old_modalite>{$f}</old_modalite><old_count>{count($e)}</old_count></c>  \n";
	xquery +="return   <r><modalite> { $elem } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>";
	//xquery +="return   <r><modalite> { data($elem) } </modalite><clioxml_initial_value>{$ori}</clioxml_initial_value><clioxml_modify>{distinct-values($atLeastOneMod)}</clioxml_modify><count>{ count($d) }</count></r>";
				
	return xquery;
}

function getXQuery_modalite_for_download(fullpath_withns) {
	var xquery="for $d in $last_collection"+fullpath_withns+" \n";
	
	var attribute=null;
	if (fullpath_withns.indexOf("@")!=-1) {
		attribute = getLastNode(fullpath_withns).substring(1);
		xquery +="let $elem:= $d \n";
	} else {
		xquery +="let $elem:= $d/text() \n";
		//xquery +="let $elem:= $d \n";
	}
	
	
	xquery +="group by $elem  \n";
	
	
	xquery +="return  <r><modalite>{ $elem } </modalite><count>{ count($d) }</count></r>";
				
	return xquery;
}