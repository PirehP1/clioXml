Ext.define('Desktop.DownloadCodage',{
	extend: 'Ext.window.Window',
	layout:'fit',
	modal:true,
	//width:600,
	width:$(window).width()*0.4,
	y:$(window).height()*0.2-100,
	//height:500,
	//height:$(window).height()-48,
	closeAction:'destroy',
	recordCodage:null,
	title:'Export Codage',
	
	
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
									var recordCodage = this.up("window").recordCodage;
									var f = this.up('form').getForm();
									var exportName = f.findField('export-name');
								
									var f = $("#formDownload");				
									f.empty();
									
									f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadCodage"));
									if (recordCodage == null) {
										f.append($("<input>").attr("type", "hidden").attr("name", "type").val("all"));										
									} else if (recordCodage.get("type")=='variable') {
										f.append($("<input>").attr("type", "hidden").attr("name", "type").val("variable"));
										f.append($("<input>").attr("type", "hidden").attr("name", "fullpath").val(recordCodage.get("fullpath")));
									} else if (recordCodage.get("type")=='codageString') {
										f.append($("<input>").attr("type", "hidden").attr("name", "type").val("codageString"));
										f.append($("<input>").attr("type", "hidden").attr("name", "pmid").val(recordCodage.get("pmid")));
									} else if (recordCodage.get("type")=='codageNumeric') {
										f.append($("<input>").attr("type", "hidden").attr("name", "type").val("codageNumeric"));
										f.append($("<input>").attr("type", "hidden").attr("name", "pmid").val(recordCodage.get("pmid")));
									} 
									f.append($("<input>").attr("type", "hidden").attr("name", "export_name").val(exportName.getValue()));
									f.submit();
									this.up("window").close();
							}
							}]
						}]
					
	
});