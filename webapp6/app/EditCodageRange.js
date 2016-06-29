Ext.define('Desktop.EditCodageRange',{
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
	title:'Edition codage Plage de valeur',
	listeners:{
		beforerender:function() {
			var f = this.down("form").getForm();
			var minValueField = f.findField('min-value');
			var maxValueField = f.findField('max-value');
			minValueField.setValue(this.recordCodage.get("minValue"));
			maxValueField.setValue(this.recordCodage.get("maxValue"));
		}
	},
	
						items:[{xtype:"form",
						
							items: [					
								{
									xtype: 'textfield',
									fieldLabel: 'Valeur min ',
									name:'min-value',
									anchor: '100%'
									
								},
								{
									xtype: 'textfield',
									fieldLabel: 'Valeur max ',
									name:'max-value',
									anchor: '100%'
									
								}
							],
							
							 buttons: [
								
							 {
								text: 'validez',
								handler: function() {
									var recordCodage = this.up("window").recordCodage;
									var f = this.up("form").getForm();
									var minValueField = f.findField('min-value');
									var maxValueField = f.findField('max-value');
									
									
									recordCodage.set("minValue",minValueField.getValue());
									recordCodage.set("maxValue",maxValueField.getValue());
									this.up("window").codageTree.save();
									this.up("window").close();
									
							}
							}]
						}]
					
	
});