Ext.define('fileupload',{
        extend: 'Ext.form.field.Text'
        ,alias: 'widget.fileupload'
        ,inputType: 'file'
        ,validateOnBlur : false
        ,listeners: {
            render: function (me, eOpts) {
                var el = Ext.get(me.id).down('input'); //<-- 4.0.7
                el.set({
                    size: me.inputSize || 1
                });
                if(me.multiple) {
                    el.set({
                        multiple: 'multiple'
                    });
                }
            }
        }
    });

    /**
* vtype
*/
    Ext.apply(Ext.form.field.VTypes, {
        file: function(val, field) {
            var input, files, file
            ,acceptSize = field.acceptSize || 4096 // default max size
            ,acceptMimes = field.acceptMimes || [ ]; // default types

            input = Ext.get(field.id).down('input'); //<-- 4.0.7
            files = input.getAttribute('files');
            if ( ! files || ! window.FileReader) {
                return true;
            }
            for(var i = 0, l = files.length; i < l; i++) {
                file = files[i];
                if(file.size > acceptSize * 1024) {
                    this.fileText = (file.size / 1048576).toFixed(1) + ' MB: invalid file size ('+(acceptSize / 1024).toFixed(1)+' MB max)';
                    return false;
                }
                var ext = file.name.substring(file.name.lastIndexOf('.') + 1);
                if(Ext.Array.indexOf(acceptMimes, ext) === -1) {
                    this.fileText = 'Invalid file type ('+ext+')';
                    return false;
                } else {
					// fichier valide,
					// todo : ajouter dans un store pour avoir la liste 
				}
            }
            return true;
        }
    });
	
Ext.define('Desktop.ImportProjetModal',{
	extend: 'Ext.window.Window',
	layout:'fit',
        width:500,
        height:300,
        closable: true,
        resizable: true,
		modal:true,
		
        //plain: true,
		
        border: true,
		title:"Importation d'un projet",
		
		
        items: [ 
                 
                 {
				xtype:'form'
				
				//,height: 150
				,fileUpload: true
				,frame: false
				,url: '/UploadProjet'
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
		               	 xtype:'textfield',                	                	 
		            	 flex:1,
		            	 required: true,
		            	 fieldLabel:"Nom du projet",
		            	 name:"projet_name"
            		 },
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
						var name = form.down("textfield").getValue();
						
						
						AmazonS3.uploadFile(field, {
							signingUrl: '/UploadProjet?projet_name='+encodeURI(name),
							successCallback: function(response) {
								Ext.getCmp('projects_list').getStore().load();
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
	
		