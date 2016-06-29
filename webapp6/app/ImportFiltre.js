
	
Ext.define('Desktop.ImportFiltre',{
	extend: 'Ext.window.Window',
	config: {
        choix_filtre:null
        
    },
	layout:'fit',
        width:500,
        height:300,
        closable: true,
        resizable: true,
		modal:true,
		filtreId:-1,
        //plain: true,
		
        border: true,
		title:'Importation de Filtre',
				
        items: [ {
				xtype:'form'
				
				//,height: 150
				,fileUpload: true
				,frame: false
				,url: '/UploadFiltre'
				//,title: 'File Upload Form'
				,bodyPadding: '10 10 0'
				//method="post" enctype="multipart/form-data"
				,defaults: {
					anchor: '100%'
					,allowBlank: false
					,labelAlign: 'top'
				}

				,items: [{
					xtype: 'fileupload'
					,vtype: 'file'
					,multiple: true // multiupload (multiple attr)
					,acceptMimes: [ 'json'] // file types
					,acceptSize: 20000
					,fieldLabel: 'Fichier  <span class="gray">(json; 20 Mo max)</span>'
					,inputSize: 76 // size attr
					,msgTarget: 'under'
					,name: 'files'
				}]

				,buttons: [{
					text: 'Upload'
					,formBind: true
					,handler: function(){
						var form = this.up('form');
						//form.setLoading(true);
						var field = form.down("fileupload");
						
						
						
						AmazonS3.uploadFile(field, {
							signingUrl: '/UploadFiltre?filtreId='+this.up("window").filtreId,
							failureCallback:function(response) {
								Ext.MessageBox.alert('Erreur', response);
								form.up("window").close();
							},
							successCallback: function(response) {	
								if (form.up("window").filtreId == -1) {
									form.up("window").choix_filtre.getStore().load();
																	
								} else {
									var tree = form.up("window").choix_filtre.up("window").down("treepanel");
									tree.getStore().removeAll(); 
									tree.getStore().load();
									
								}
								form.up("window").choix_filtre.up("window").app.fireEvent("filtreUpdated",this,form.up("window").filtreId);
								form.up("window").close();
								 	
							}
						});
						
						return;
						
						
					}
				}
				
				]
			}
		]
	});
	
	