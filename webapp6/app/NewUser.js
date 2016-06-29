
	
Ext.define('Desktop.NewUser',{
	extend: 'Ext.window.Window',
	updateUser:function () {
		var user=this.user;
		console.log("user=",user);
		this.setTitle('Modification utilisateur');
		var f = this.down('form').getForm();
		var rw = false;
		var projet_unique=-1;
		if (user.data['credential']!=null) {
			rw = user.data['credential'].readwrite;
			projet_unique = user.data['credential'].projet_unique;
		}
		
		
		f.setValues({
			identifiant: user.data['email'],
			nom: user.data['lastname'],
			prenom: user.data['firstname'],
			readwrite : rw,
			projet:projet_unique
			})
			
		/*
			f.findField('identifiant').setValue(user.data['identifiant']);
			f.findField('nom').setValue(user.data['nom']);
			f.findField('prenom').setValue(user.data['prenom']);
			//f.findField('password').setValue(user.data['identifiant']);
			if (user.data['credential']!=null) {
				f.findField('readwrite').setValue(user.data['credential'].readwrite);
			}
			*/
	},
	user:null,
	layout:'fit',
        width:500,
        height:300,
        closable: true,
        resizable: true,
		modal:true,
		
        border: true,
		title:'Nouvel utilisateur',
		listeners: {
			afterrender:function() {						
						if (this.user!=null) {
							this.updateUser();
						}
						
			}
		}, // fin listeners		
        items: [
                {xtype:"form",
                	items:[
                {xtype:'textfield',                	                	 
                	anchor: '90%',
            	 required: true,
            	 fieldLabel:"identifiant",
            	 name:"identifiant"},
            	 
            	 {xtype:'textfield',                	                	                 	
            	 required: true,
            	 anchor: '90%',
            	 fieldLabel:"Nom",
            	 name:"nom"},
                	 
            	 {xtype:'textfield',                	                	                      	
            	 required: true,
            	 anchor: '90%',
            	 fieldLabel:"Prénom",
            	 name:"prenom"},
            	 {xtype:'textfield',                	                	                      	
                	 required: true,
                	 anchor: '90%',
                	 fieldLabel:"Mot de passe",
                	 name:"password"},
            	 
            	 {xtype:'checkbox',                	                	                      	
                	 
                	 anchor: '90%',
                	 fieldLabel:"Accès écriture",
                	 name:"readwrite"},
            	 {
                		 xtype:'combobox',                	                	                      	
                    	 required: false,
                    	 allowBlank:true,
                    	 anchor: '90%',
                    	 fieldLabel:"projet unique",
                    	 name:"projet",
                    	 itemId:"projectCombo",
                         valueField: 'id',
                         displayField: 'name',
                         editable: false
                         //queryMode:"local",
                         //mode:"local"
            	 }
            	 ], // items de form
            	 listeners : {
     				render:function() {
     					//var thegrid = this;
     					//thegrid.setLoading(true);
     					
     					var me=this;
     					var projects = Ext.create('Desktop.ProjectStore');
     					projects.on("load",function(store, records, successful, eOpts) {
     							
     							store.insert(0, [{id: -1, name: "non"}]);
     							
     							me.down("#projectCombo").setStore(store);
     							
     							}
     							);
     					
     				}
      			}, // listeners
                buttons : [
                 		  {   text:"Valider",
                 			  listeners:{										
                 					'click':function(button) {
                 						
                 						var f = button.up('form').getForm();
                 						
                 						var identifiant = f.findField('identifiant').getValue();
                 						var nom = f.findField('nom').getValue();
                 						var prenom = f.findField('prenom').getValue();
                 						var password = f.findField('password').getValue();
                 						var readwrite = f.findField('readwrite').getValue();
                 						var projet_unique = f.findField('projet').getValue();
                 						
                 						//console.log(identifiant,nom,prenom,readwrite);
                 						button.up("window").setLoading(true);
                 						var command = "createUser";
                 						var id="";
                 						if (button.up("window").user !=null) {
                 							command = "updateUser";
                 							id = button.up("window").user.data['id'];
                 						}
                 						$.get("/service/commands?cmd="+command,{id:id,identifiant:identifiant,nom:nom,prenom:prenom,password:password,projet_unique:projet_unique,readwrite:readwrite},function(response) {
    										button.up("window").setLoading(false);
    										if ("error" in response && response.error != '') {											
    											alert("erreur",response.error);
    										} else {
    											//todo reload users list
    											button.up("window").grid.getStore().reload();
    											button.up("window").close();
    											
    										}
    									});	
                 					}
                 				}
                 			}
                 		  
                 		] // buttons
                } //form
		],
		
	});
	
	