Ext.define('Desktop.LoginWindow',{
	extend: 'Ext.window.Window',
	layout:'fit',
	modal:true,
	//width:600,
	width:$(window).width()*0.4,
	y:$(window).height()*0.2-100,
	//height:500,
	//height:$(window).height()-48,
	//closeAction:'destroy',
	closable: false,
	title:'Login',
	
	
						items:[{xtype:"form",
						
							items: [					
								{
									xtype: 'textfield',
									fieldLabel: 'Login',
									name:'login',
									anchor: '100%',
									tabIndex:1
									
								},
								{
									xtype: 'textfield',
									fieldLabel: 'Mot de passe',
									name:'password',
									inputType: 'password',
									anchor: '100%',
									tabIndex:1
									
								}
							],
							
							 buttons: [
								
							 {
								text: "s'identifier",
								handler: function(w,a,b,c) {
									
									var f = w.up('form').getForm();
									
									var login = f.findField('login').getValue();
									var password = f.findField('password').getValue();
									
									
									w.up("window").setLoading(true);
									
									var me=w;
									var win = me.up("window");
									$.get("/service/commands?cmd=login",{user:login,password:password},function(response) {
										me.up("window").setLoading(false);
										if ("error" in response && response.error != '') {											
											alert("mauvaise identification");
										} else {
											var user = response;
											me.up("window").close();
											if (user.id == 2) { // admin
												new Desktop.AdminApp();	
												
												/*
												me.up("window").close();
												var win = Ext.create('Desktop.AdminDBManager');
												
												win.app = me.app;
												win.show();
												var win2 = Ext.create('Desktop.AdminUserManager');
												
												win2.app = me.app;
												win2.show();
												*/
											} else { // classic user
												var app = new Desktop.App();
												console.log("logged user :",user);
												app.user = user;
											}
										}
									});	
									//
							}
							}]
						}]
					
	
});