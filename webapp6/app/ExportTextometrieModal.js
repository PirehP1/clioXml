

Ext.define('Desktop.ExportTextometrieModal',{
	extend: 'Ext.window.Window',
	
	modal:true,
	
	width:$(window).width()*0.5,
	y:$(window).height()*0.2-100,
	
	closeAction:'destroy',
	
	title:'Export Textometrie',
	cols:null,
	subcols:null,
	filtreId:-1,
	getData:function() {
		var c=[];
		for (var i=0;i<this.cols.length;i++) {
			c.push({path:this.cols[i],nom:getLastNode(this.cols[i]),type:'ignoré'});
		}
		return c;
		
	},
	
	initComponent: function() {
		Ext.apply(this, { 
			layout:'fit',
			items:[
					{
						xtype: 'grid',
						selType: 'rowmodel',
						plugins: [
							Ext.create('Ext.grid.plugin.CellEditing', {
								clicksToEdit: 1								
							})
						],	
						store: new Ext.data.JsonStore({
							fields: ['path','nom','type'],							
							data:this.getData()
							//,autoSync:true
						} ),
						viewConfig:{
						    markDirty:false
						},
						columns: [{
								header: 'Path',
								dataIndex: 'path',
								flex: 1,
								renderer:function (value, metadata, record, rowIndex, colIndex, store,view) {									
									var p = record.get("path");
									return removeQName(p);
									
								}
							}, {
								header: 'Nom',
								dataIndex: 'nom'																	
							}
							
							, 
							{
								dataIndex: 'type',
								header: 'type',
								renderer:function (value, metadata, record, rowIndex, colIndex, store,view) {									
									var type = record.get("type");
									if (type==='') {
										return 'à renseigner';
									} else {
										return type;
									}
									
								},
								
								getEditor: function(record){	
									var comp = Ext.create('Ext.grid.CellEditor', {								
										field: {
											xtype:"combo", 
											editable:false,
											forceSelection:false,
											emptyText:'à renseigner',
											value:record.get("type"),
											store:['ignoré','variable','texte'],
											enableKeyEvents: true,
											listeners: {															
												blur:function( field, event, eOpts ) {
													
													record.set("type",field.getValue());	
													record.commit();
													
													//record.getData().type=field.getValue();
													//record.getStore().load();
												},
												
												specialkey: function (field, e) {
														if (e.getKey() == e.ENTER) {
															//field.setValue(field.getRawValue());
															
															record.set("type",field.getValue());
															record.commit();
															//record.getStore().load();
														}
													}
													
											} // listeners
											
											}
									});
									return comp;
									
								}
							}
							] // columns
					}
					
				]
			,
			bbar:[
				{
					xtype: 'cycle',
					text: '',
					prependText: 'format: ',
					showText: true,
					scope: this,
					
					menu: {												
						items: [{
							text: 'lexico3',
							checked: true													
						}, {
							text: 'hyperbase'													
						}, {
							text: 'iramuteq'													
						}, {
							text: 'txm'													
						}]
					}
				},
				{
					text:"Exporter",handler:this.onExport,scope:this
				}
			]
			
			}); // ext.apply
			this.callParent();

			} // initComponent
	
	
	
	,onExport:function() {		
		
		var store = this.down("grid").getStore();
		var r = store.getRange();
		
		var textoparams=[];
		var colonnes = [];
		for (var i=0;i<r.length;i++) {
			
			if (r[i].get("type")=='ignoré') {
				continue;
			}
			colonnes.push(r[i].get("path"));
			
			textoparams.push({text:r[i].get("nom"),type:r[i].get("type")});
		}
		
		if (textoparams.length!=2) {
			
			alert("veuillez sélectionner que 2 noeuds");
			console.log("textoparams",textoparams);
			return;
		}
		
		var format=this.down("cycle").getActiveItem().text;
		
		
		 var f = $("#formDownload");				
		f.empty();
		
		f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("exportTextometrie"));			
		f.append($("<input>").attr("type", "hidden").attr("name", "colonnes").val(JSON.stringify(colonnes)));
		f.append($("<input>").attr("type", "hidden").attr("name", "subcols").val(JSON.stringify(textoparams)));
		f.append($("<input>").attr("type", "hidden").attr("name", "format").val(format));
		f.append($("<input>").attr("type", "hidden").attr("name", "nomExport").val("export_textometrie"));
		f.append($("<input>").attr("type", "hidden").attr("name", "filtreId").val(this.filtreId));
		f.submit();
		 
		this.close();
	}
	
		 
	
	
});
