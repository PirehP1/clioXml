Ext.define('Desktop.ImportXsltModal',{
	extend: 'Ext.window.Window',
	layout:'fit',
        width:500,
        height:300,
        closable: true,
        resizable: true,
		modal:true,
		
        //plain: true,
		
        border: true,
		title:"Importation des feuilles de styles",
		
		
        items: [ 
                 
                 {
				xtype:'form'
				
				//,height: 150
				,fileUpload: true
				,frame: false
				,url: '/UploadFeuillesStyle'
				//,title: 'File Upload Form'
				,bodyPadding: '10 10 0'
				//method="post" enctype="multipart/form-data"
				,defaults: {
					anchor: '100%'
					,allowBlank: false
					,labelAlign: 'top'
				}

				,items: [
				        
				         {
					xtype: 'fileupload'
					,vtype: 'file'
					,multiple: true // multiupload (multiple attr)
					,acceptMimes: [ 'zip'] // file types
					,acceptSize: 100000
					,fieldLabel: 'Fichier  <span class="gray">(zip; 100 Mo max)</span>'
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
							signingUrl: '/UploadFeuillesStyle',
							successCallback: function(response) {
								// TODO : reload xslt list
								Ext.getCmp('listXslt').getStore().load();
								form.up("window").close();
							}
						});
						
						return;
						
						
					}
				}
				/*
				,{
					text: 'Reset'
					,handler: function() {
						this.up('form').getForm().reset();
					}
				}*/
				]
			}
		]
	});
	
		