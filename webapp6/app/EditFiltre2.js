var operateurMap = [
	{'id':'eq','name':'=','type':'all'},
	{'id':'ne','name':'!=','type':'all'},
	{'id':'lt','name':'<','type':'all'},
	{'id':'gt','name':'>','type':'all'},
	{'id':'lte','name':'<=','type':'all'},
	{'id':'gte','name':'>=','type':'all'},
	
	{'id':'contains','name':'contient','type':'string'},
	{'id':'startswith','name':'commence par','type':'string'},
	{'id':'endswith','name':'termine par','type':'string'},
	{'id':'matches','name':'matches','type':'string'}

	];
function formatOperateur(op) {
	
	for (var i=0;i<operateurMap.length;i++) {
		
		var o = operateurMap[i];
		
		if (o['id'] == op) {
			return $("<div>").text(" "+o['name']+" ").html();
		}
	}
	return "??";
}
// http://dev.sencha.com/ext/5.0.1/examples/kitchensink/#cell-editing
Ext.define('Desktop.EditFiltre2',{
	extend: 'Ext.window.Window',
	
	modal:false,
	
	width:$(window).width()*0.5,
	y:$(window).height()*0.2-100,
	
	closeAction:'destroy',
	treeWindow:null,
	title:'Edition Filtre',
	callback:null,
	node:null,
	listeners : {
		close:function() {
			this.treeWindow.unmask();
		}
	},
	getModifierStore:function() {
		return ['number','abs','ceiling','floor','round','round-half-to-event','count','node-name','nilled','data','string-length','normalize-space','upper-case','lower-case'];
		// add : function on date and time, 
	},
	getOperatorStore:function(node_type) {
		return new Ext.data.Store({
							autoLoad:false,
							fields: ['id','name'],
							data:operateurMap
						} )
		// TODO retourne la liste des operateurs suivant le type du noeud , prendre dans operateurMap
		
	},
	getConditions:function() {
		var cs = [];
		for (var i=0;i<this.node.conditions.length;i++) {
			var o=this.node.conditions[i];
			cs.push({type:o.type,modifiers:o.modifiers.slice(),operator:o.operator,value1:o.value1,value2:o.value2});
		}
		
		return cs;
	},
	initComponent: function() {
		Ext.apply(this, { 
			layout:'fit',
			items:[{
				xtype:'form',
				//layout: 'column',
				items:[
					{
						xtype: 'gridpanel',
						store: new Ext.data.JsonStore({
							fields: ['type','modifiers','operator','value1','value2'],
							//data:this.node.conditions // slice pour avoir un clone et non pas le vrai tableau de node
							data:this.getConditions()
						} ),
						listeners: {
							scope: this,
							selectionchange: this.onSelectionChange
						},
						columns: [{
								header: 'Modifieurs',
								dataIndex: 'modifiers',
								flex: 1,
								renderer : function (value, metadata, record, rowIndex, colIndex, store,view) {		
									
									var w = view.up("window");
									
									var s=getLastNode(w.node.node);
									for (var i=0;i<value.length;i++) {
										s = value[i]+"("+s+")";
									}
									return s;
								}
							}, {
								header: 'operateur',
								dataIndex: 'operator',									
								renderer : function (value, metadata, record, rowIndex, colIndex, store,view) {		
									return formatOperateur(value);
								}
							}, {
								header: 'Valeur1',
								dataIndex: 'value1'						
							},
							
							{
								header: 'Valeur2',
								dataIndex: 'value2'						
							}, {
								xtype: 'actioncolumn',
								width: 30,
								sortable: false,
								menuDisabled: true,
								items: [{
									icon: 'resources/images/remove.png',
									tooltip: 'supprimer',
									scope: this,
									handler: this.onRemoveClick
								}]
							}] // columns
					},
					{
						xtype:"button",text:'nouvelle condition',handler:this.onAddClick,scope:this
					},
					{
						xtype: 'fieldset',
						disabled : true,
						//layout: 'anchor',
						title:'Edition',
						margin: '10 10 10 10',
						defaultType: 'textfield',
						defaults: {
							listeners: {
								change: this.onChange,
								scope:this
							}
						},
						items: [{
							fieldLabel: 'modifieurs',
							xtype:'itemselector',
							store: this.getModifierStore(),
							name: 'modifiers',
							height:200
							
						},{
							fieldLabel: 'operateur',
							displayField:'name',
							valueField:'id',
							name: 'operator',
							xtype:'combo',
							editable:false,
							store:this.getOperatorStore()
							
						},{
							fieldLabel: 'valeur 1',
							name: 'value1'
						},{							
							fieldLabel: 'valeur 2',
							name: 'value2'
						}
						/*
						,{							
							text:"valider",
							xtype: 'button',
							handler:this.onUpdateClick,
							scope: this
						}
						*/
						]
					}
				]
			}],
			bbar:[
				{
					text:"Appliquer les modifications",handler:this.onValid,scope:this
				}
			]
			
			}); // ext.apply
			this.callParent();

			} // initComponent
	
	,onAddClick: function() {
			console.log("addClick");
		//TODO : add a new  record into the store
			// and select it 
			var grid = this.down("gridpanel");
			grid.getStore().add({type:"condition",modifiers:[],operator:"eq",value1:"",value2:""});
			
	}
	,onSelectionChange: function(model, records) {
        var rec = records[0];
        if (rec) {
			var f = this.down("form");
			
            f.getForm().loadRecord(rec);
			this.down("fieldset").setDisabled(false);
			//disabled : true,
        }
    }
	,onRemoveClick: function(grid, rowIndex){		
		var grid = this.down("gridpanel");
        grid.getStore().removeAt(rowIndex);
		
    }	
	,onChange : function(field, newVal, oldVal) {
		var f = this.down("form");		
		var iRecord = f.getForm().getRecord();
		iRecord.set(field.name,newVal);
		
	}
	,onValid:function() {		
		var grid = this.down("gridpanel");
        var s = grid.getStore().getRange();
		console.log("s=",s);
		/*
		for(var i=0;i<this.node.conditions.length;i++) {
			delete this.node.conditions[i].id;
		}
		*/
		var new_conditions = [];
		for (var i=0;i<s.length;i++) {
			delete s[i].data.id;
			new_conditions.push(s[i].data);			
		}
		this.callback(new_conditions);
		this.close();
	}
	
	/*
	,onUpdateClick: function() {
		console.log("onUpdateClick");
        var f = this.down("form");		
		var iRecord = f.getForm().getRecord(),
            iValues = f.getForm().getValues(),
			iFieldValues = f.getForm().getFieldValues();
			

		
		//iRecord.set("modifiers",iValues.modifiers.split(","))
        iRecord.set( iFieldValues );
		
        //this.getUsersStore().insert(0, iRecord);
		

			console.log("iRecord=",iRecord);
    }
*/	
	/*
	listeners:{
		beforerender:function() {
			this.cellEditing = new Ext.grid.plugin.CellEditing({
            clicksToEdit: 1
        });

        var panel = Ext.create('Ext.grid.Panel',{
			xtype: 'cell-editing',
            
            plugins: [this.cellEditing],
            store: new Ext.data.JsonStore({
				fields: ['type','modifiers','operator','value1','value2'],
				data:this.node.conditions
			} ),
			columns: [{
                header: 'Modifiers',
                dataIndex: 'common',
                flex: 1,
                editor: {
                    allowBlank: true
					
                }
            }, {
                header: 'operator',
                dataIndex: 'operator',
                width: 130,
                editor: this.getOperatorEditor()
            }, {
                header: 'Valeur1',
                dataIndex: 'value1',
                width: 70,
                align: 'right',                
                editor: {
                    xtype: 'textfield',
                    allowBlank: false                   
                }
            },
			
			{
                header: 'Valeur2',
                dataIndex: 'value2',
                width: 70,
                align: 'right',                
                editor: {
                    xtype: 'textfield',
                    allowBlank: false                   
                }
            }, {
                xtype: 'actioncolumn',
                width: 30,
                sortable: false,
                menuDisabled: true,
                items: [{
                    icon: 'resources/images/icons/fam/delete.gif',
                    tooltip: 'supprimer',
                    scope: this,
                    handler: this.onRemoveClick
                }]
            }],
            selModel: {
                selType: 'cellmodel'
            },
            tbar: [{
                text: 'Ajouter une condition',
                scope: this,
                handler: this.onAddClick
            }]
        });
		this.add(panel);
		
		var itemSe = Ext.create('Ext.ux.ItemSelector',{store: ['A','B','C']});

							
		this.add(itemSe);		
			
		},
		close:function() {
			this.treeWindow.unmask();
		}
	},
    

    onAddClick: function(){
		console.log("add");
        // Create a model instance
		
    },

    onRemoveClick: function(grid, rowIndex){
        this.getStore().removeAt(rowIndex);
    }
	
					*/	 
	
	
});
