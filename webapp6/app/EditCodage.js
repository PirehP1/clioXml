Ext.define('Desktop.EditCodage',{
	extend: 'Ext.window.Window',
	layout:'fit',
	modal:true,
	codateType:null,
	width:$(window).width()*0.4,
	y:$(window).height()*0.2-100,
	
	closeAction:'destroy',
	recordCodage:null,
	title:'Edition codage',
	listeners:{
		beforerender:function() {
			if (this.recordCodage.get("type")=='range') {
				this.getItemsForRange();
			}
			
		}
	},
						 
					
	getItemsForRange:function() {
		var form = Ext.create("Ext.form.Panel",{
				trackResetOnLoad :true,
				items: [					
					{
						xtype: 'numberfield',
						fieldLabel: 'Valeur min ',
						name:'min-value',
						anchor: '100%'
						
					},
					{
						xtype: 'numberfield',
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
						
						if (f.isDirty()) {							
							/*
							var minValueField = f.findField('min-value');
							var maxValueField = f.findField('max-value');														
							recordCodage.set("minValue",minValueField.getValue());
							recordCodage.set("maxValue",maxValueField.getValue());
							*/
							var values = f.getValues();
							recordCodage.set("minValue",values['min-value']);
							recordCodage.set("maxValue",values['max-value']);
							this.up("window").codageTree.save();
						}
						this.up("window").close();
						
				}
				}]
			});
			this.add(form);
			var f = form.getForm();
			f.setValues({'min-value':this.recordCodage.get("minValue"),'max-value':this.recordCodage.get("maxValue")});			
			f.reset(); // to have no dirty flag, works with trackResetOnLoad
			
			
						
	},
	getItemsForModalite:function() {
		
	}
});