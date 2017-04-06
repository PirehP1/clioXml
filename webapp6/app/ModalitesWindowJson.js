
    Ext.define('ModaliteJsonModel',{ // was book
        extend: 'Ext.data.Model',
		requires:[
			
		],
        fields: [            
            'modalite', {name:'count',type:"integer"},'pm'
			]        
    });

	Ext.define('ModalitesJsonStore', {	
		extend: 'Ext.data.Store',
        model: 'ModaliteJsonModel'        ,
        autoLoad: true,
        proxy: {
            type: 'memory',
            reader: {
                type: 'json'
            }
        }
    })
    
    Ext.define('ModaliteCountJsonModel',{ // was book
        extend: 'Ext.data.Model',
		requires:[
			
		],
        fields: [            
            {name:'totaloccurence',type:"integer"},
            {name:'distinctmod',type:"integer"},
            {name:'fichenotfound',type:"integer"},
            {name:'fichefound',type:"integer"}
			]        
    });

	Ext.define('ModalitesCountJsonStore', {	
		extend: 'Ext.data.Store',
        model: 'ModaliteCountJsonModel'        ,
        autoLoad: true,
        proxy: {
            type: 'memory',
            reader: {
                type: 'json'
            }
        }
    })
    
    


Ext.define('Desktop.ModalitesWindowJson', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'modalites-json-win',

    init : function(){
        this.launcher = {
            text: 'Distribution',
            iconCls:'icon-grid',
            idmenu:"requete"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var theapp = this.app;
           var win = desktop.createWindow({
                itemId: 'mod-json-win',
                title:'Distribution',
				y:0,
				x:$(window).width()*0.3,
                width:$(window).width()*0.4,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				//layout: 'fit',
                layout:'border',
				app:theapp,		
				
				execFromHisto(data) {
					var tf = this.down('#newCol');
					var tf2 = this.down("#refCol");
					
					tf.setValue(removeQName(data.params.path));
					tf2.setValue(removeQName(data.params.ref_path));
					
					var grid = this.down("#list_mods");
					grid.current_path = data.params.path ; //getFullPath_from_array(els);
					grid.current_path_type = data.params.path_type;
					grid.ref_path = data.params.ref_path;
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
					        /*
						{
							xtype: 'grid',
						    itemId:'count_mods',
						    //flex:1,
						    height:70,
						    region:'north',
							store : Ext.create('ModalitesCountJsonStore')
							
						},
						*/
					    {
					    	xtype:'textfield',
					    	disabled:true,
					    	height:35,
							value:'',
							region:'north',
							//fieldStyle: 'background-color: red !important;color:black !important;',
							//flex:1,
							//grow:true,
							itemId:'count_mods_field',
							fieldCls:"count_mods_field"
					    },
                    {
						current_filtreId:-1,
                        border: false,
                        region:'center',
                        xtype: 'grid',
                        itemId:'list_mods',
						store : Ext.create('ModalitesJsonStore'),
						selModel: {
							mode: 'MULTI',
							allowDeselect: true
						},
						current_path:null,
						ref_path:null,
						totalEffectif:1,
						columns: [
							{
								text: "modalite", flex: 1, dataIndex: 'modalite',
								
								renderer:function(value, d) {			
									var icon="";
									
									if (d.record.get('pm')!=null && d.record.get('pm')!='') {
										icon = '<img src="resources/images/icon_c.jpg"/>';
									}
									
									return value+icon;
								}
							},
							
							{text: "recodage", flex: 1, dataIndex: 'pm'},
							{text: "count", flex:1, dataIndex: 'count'},
							{
								text: "fréquence", flex: 1, dataIndex: 'count',
								
								renderer:function(value) {
									
									return value/this.totalEffectif;
								}
							}
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
										  me.load_data(true);
										
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
								var thegrid = this.up("window").down('#list_mods');
								var statsGrid = this.up("window").down("#count_mods");
								var statsField = this.up("window").down("#count_mods_field");
								
								
								var dd = new Ext.dd.DropTarget(tf.getEl(), {
									// must be same as for tree
									 ddGroup:'t2div'

									
									
									,notifyDrop:function(dd, e, node) {
										//console.log(node.records[0]);
										if (!node.records[0].data.leaf) {
											Ext.Msg.alert('Attention', "Vous comptez l'agregat de noeuds");
											
											
											node.records[0].expand();
											//return;
										}
										
										var els = schemaNode_to_array(node);
										
										tf.setValue(getFullPath_from_array(els));
										
										
										var m = thegrid;
										
										/*
										Ext.suspendLayouts();
										var element = els[els.length-1].name;
										var newColumns=[
											{text: "Effectif total de "+element, dataIndex: 'totaloccurence',flex:1},
											{text: "Nombre de modalités de "+element,  dataIndex: 'distinctmod',flex:1},
											{text: "Nombre de "+element+" absents",  dataIndex: 'fichenotfound',flex:1},
											{text: "Nombre de "+element+" présents",  dataIndex: 'fichefound',flex:1}];
										
										
										statsGrid.reconfigure(statsGrid.getStore(),newColumns);
										Ext.resumeLayouts(true);
										*/
										
										m.current_path = getFullPathNS_from_array(els) ; //getFullPath_from_array(els);
										m.current_path_type = node.records[0].data.type;
										//console.log("type is",m.current_path_type);
										
										m.load_data(true);
												
										return true;
									} // eo function notifyDrop
								}); // fin dropTarget
								
								var tf2 = this.up("window").down('#refCol');
								
								//var therecodage = this.up("window").down('treepanel');
								var dd = new Ext.dd.DropTarget(tf2.getEl(), {
									// must be same as for tree
									 ddGroup:'t2div'

									
									
									,notifyDrop:function(dd, e, node) {										
										
										var els = schemaNode_to_array(node);
										
										tf2.setValue(getFullPath_from_array(els));
										
										
										var m = thegrid;
										m.ref_path = getFullPathNS_from_array(els) ; //getFullPath_from_array(els);
										
										//console.log("type is",m.current_path_type);
										
										m.load_data(true);
												
										return true;
									} // eo function notifyDrop
								});
								
							}
						},
						load_data:function(addToHisto) {
							
							var m = this;
							if (m.current_path == null || m.ref_path==null) return;
							var store = m.getStore();
							
							var url="/service/commands?cmd=getListModalitesJson";
							
							/*
							store.getProxy().setUrl(url);
							store.getProxy().extraParams ={path:m.current_path,filtreId:m.current_filtreId};
							
							store.load();
							*/
							m.setLoading(true);
							var w = m.up("window");
							var start = (w.currentPage-1)*w.nb_result_per_page+1;
							var end = start + w.nb_result_per_page;
							
							$.post(url,{start:start,end:end,refpath:m.ref_path,path:m.current_path,filtreId:m.current_filtreId},function(response) {
								//console.log("result !",response);
                                //var j = JSON.parse(response);
								var j = response;
                                w.totalPage = Math.floor(j.counts.distinctmod / w.nb_result_per_page);
                                if ((j.counts.distinctmod % w.nb_result_per_page)>0) {
                                	w.totalPage = w.totalPage+1;
                                }
                                var store = m.getStore();
                                m.totalEffectif = j.counts.fichenotfound+j.counts.fichefound; //j.counts.totaloccurence;
                                store.loadData(j.modalites,false); 
                                
                                /*
                                var s2 = m.up("window").down('#count_mods').getStore();
                                s2.loadData([j.counts],false);
                                */
                                /*
                                {text: "Effectif total de "+element, dataIndex: 'totaloccurence',flex:1},
								{text: "Nombre de modalités de "+element,  dataIndex: 'distinctmod',flex:1},
								{text: "Nombre de "+element+" absents",  dataIndex: 'fichenotfound',flex:1},
								{text: "Nombre de "+element+" présents",  dataIndex: 'fichefound',flex:1}];
								*/
                                
                                
                                													
								var element = getLastNode(m.current_path);
								var variable = getLastNode(m.ref_path);
								
                                var txt = [];
                                txt.push("Effectif total de '"+element+"' : "+j.counts.totaloccurence);
                                txt.push("Nombre de modalités de '"+element+"' : "+j.counts.distinctmod);
                                txt.push("Nombre de '"+element+"' absents : "+j.counts.fichenotfound);
                                txt.push("Nombre de '"+element+"' présents : "+j.counts.fichefound);
                                txt.push("Effectif de '"+variable+"' : "+(m.totalEffectif))
				
                                m.up("window").down('#count_mods_field').setValue(txt.join("; \t")); 
                                
                                
                                m.up("window").down("#pages").setData(w.currentPage+"/"+w.totalPage);
                                m.setLoading(false);
                                
							});

							
							if (addToHisto) {
								var combo = this.up("window").down("#choix_filtre");
								
								var filtreName = "";
								if (m.current_filtreId!=-1) {
									filtreName = combo.getStore().findRecord('id',m.current_filtreId).data.name;
								}
								m.up("window").app.addToHisto({from:"modalite",type:"query",params:{ref_path:m.ref_path,path_type:m.current_path_type,path:m.current_path,filtreName:filtreName,filtreId:m.current_filtreId},timestamp:null}); //{from:"modalite",type:"query",params:{},timestamp:null}
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
                gotoPage:function(p) {
                	this.currentPage = p;
                	var m = this.down("#list_mods");
                	m.load_data(false);
                },
                currentPage:1,
				totalPage:1,
				nb_result_per_page:50,
				
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
                dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						items: [
							{
								text:"export",
								listeners:{
									
									'click':function(button) {
										//alert("todo 1"); return;
										var f = $("#formDownload");				
										f.empty();
										
										f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadModalitesJson"));
										var current_path = button.up('window').down("#list_mods").current_path;
										var refpath = button.up('window').down("#list_mods").ref_path;
										var filtreId = button.up('window').down("#list_mods").current_filtreId;
										f.append($("<input>").attr("type", "hidden").attr("name", "path").val(current_path));
										f.append($("<input>").attr("type", "hidden").attr("name", "refpath").val(refpath));
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
										var url="/service/commands?cmd=getXQueryListModalitesJson";
										var m = button.up('window').down("#list_mods");
										$.post(url,{refpath:m.ref_path,path:m.current_path,filtreId:m.current_filtreId,start:1,end:25},function(response) {
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
							}
							
						]
					},
					
					{
						xtype: 'toolbar',
						dock: 'top',
						items: [
							{xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'regroupement des modalités',itemId:"newCol",editable:false,emptyText:'coller un noeud xml'}     
						]
					},
					
					{
						xtype: 'toolbar',
						dock: 'top',
						items: [
							{xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'noeud dénombré',itemId:"refCol",editable:false,emptyText:'coller un noeud xml'}
						   
						]
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
			//els.push({name:"@"+currentNode.data.name,name_ns:"@"+currentNode.data.name});
			els.push({name:currentNode.data.name,name_ns:currentNode.data.name});
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
			els.push({name:currentNode.data.name,name_ns:currentNode.data.name});	// "@"+		
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