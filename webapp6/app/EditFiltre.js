var operateurMap = [
	{'id':'eq','name':'=','type':'all'},
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
Ext.define('Desktop.EditFiltre',{
	extend: 'Ext.window.Window',
	layout:'fit',
	modal:false,
	
	width:$(window).width()*0.4,
	y:$(window).height()*0.2-100,
	
	closeAction:'destroy',
	treeWindow:null,
	title:'Edition Filtre',
	callback:null,
	condition:null,
	
	listeners:{
		beforerender:function() {
			
				this.getItemsForRange();
				
			
		},
		close:function() {
			this.treeWindow.unmask();
		}
	}
						 
		,			
	getItemsForRange:function() {
		var form = Ext.create("Ext.form.Panel",{
				trackResetOnLoad :true,
				checkButton:function() {
					var button = this.down("#validButton");
					
					var f = this.getForm();
						
						var operateur = f.findField('operator').getValue();
						var rightpart = f.findField('rightpart').getValue();
						console.log("opertaeur = ",operateur);
						console.log("rightpart = ",rightpart);
						if (operateur==null || operateur=="" || rightpart == null || rightpart=="" ) {
							button.setDisabled(true);
						} else {
							button.setDisabled(false);
						}
					
				},
				items: [					
					{
						xtype: 'textfield',
						fieldLabel: 'Variable',
						name:'leftpart',
						anchor: '100%',
						readOnly: true 
					},
					{
						xtype:'combo',
						fieldLabel: 'operateur',
						name:'operator',
						anchor: '100%',
						displayField:'name',
							 valueField:'id',		
							 editable:false,
							 store:new Ext.data.Store({
								fields: ['id','name'],
								data : operateurMap
							})
						,
						listeners: {
							'change': function(){
							  this.up("panel").checkButton();
							}
						  }
					},
					{
						xtype: 'textfield',
						fieldLabel: 'valeur',
						name:'rightpart',
						anchor: '100%',
						listeners: {
							'change': function(){
							  this.up("panel").checkButton();
							}
						  }
						
					}
				],
				buttons: [
					
				 {
					text: 'validez',
					disabled:true,
					itemId:'validButton',
					handler: function() {
						var w = this.up("window");
						//w.condition.value="xxxxxxxxxxx";
						var f = this.up("form").getForm();
						
						w.condition.operator = f.findField('operator').getValue();
						w.condition.rightpart = {"type":"value","value":f.findField('rightpart').getValue(),"modifiers":null};
						w.callback(w.condition);
						w.close();
						
						
						
				}
				}]
			});
			var w = this;
			
			this.add(form);
			var f = form.getForm();
			f.setValues({'leftpart':formatPart(w.condition.leftpart),'operator':w.condition.operator,'rightpart':formatPart(w.condition.rightpart)});
			//f.setValues({'min-value':this.recordCodage.get("minValue"),'max-value':this.recordCodage.get("maxValue")});			
			f.reset(); // to have no dirty flag, works with trackResetOnLoad
			form.checkButton();
			
						
	},
	getItemsForModalite:function() {
		
	}
});