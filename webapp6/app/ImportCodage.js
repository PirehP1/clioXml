
	
Ext.define('Desktop.ImportCodage',{
	extend: 'Ext.window.Window',
	config: {
        parentCaller:null
        
    },
	layout:'fit',
        width:500,
        height:300,
        closable: true,
        resizable: true,
		modal:true,
		
        //plain: true,
		
        border: true,
		title:'Importation de codage',
		
		
        items: [ {
				xtype:'form'
				
				//,height: 150
				,fileUpload: true
				,frame: false
				,url: '/UploadCodage'
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
							signingUrl: '/UploadCodage',
							failureCallback:function(response) {
								Ext.MessageBox.alert('Erreur', response);
								form.up("window").close();
							},
							successCallback: function(response) {
								
								form.up("window").parentCaller.load_codage();
								form.up("window").parentCaller.up("window").app.fireEvent("codageUpdated",this);
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
	
	