Ext.define('Desktop.ExportFiltre',{
	extend: 'Ext.window.Window',
	layout:'fit',
	modal:true,
	width:$(window).width()*0.4,
	y:$(window).height()*0.2-100,	
	closeAction:'destroy',
	filtreId:-1,
	positionConstraint :-1,
	title:'Export Filtre',
	
	
						items:[{xtype:"form",
						
							items: [					
								{
									xtype: 'textfield',
									fieldLabel: 'Nom ',
									name:'export-name',
									anchor: '100%'
									
								}
							],
							
							 buttons: [
								
							 {
								text: 'exporter',
								handler: function() {
									var filtreId = this.up("window").filtreId;
									var positionConstraint = this.up("window").positionConstraint;
									
									var f = this.up('form').getForm();
									var exportName = f.findField('export-name');
								
									var f = $("#formDownload");				
									f.empty();
									
									f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("exportFiltre"));
									f.append($("<input>").attr("type", "hidden").attr("name", "filtreId").val(filtreId));
									f.append($("<input>").attr("type", "hidden").attr("name", "positionConstraint").val(positionConstraint)); 
										
									
									f.append($("<input>").attr("type", "hidden").attr("name", "export_name").val(exportName.getValue()));
									f.submit();
									this.up("window").close();
							}
							}]
						}]
					
	
});