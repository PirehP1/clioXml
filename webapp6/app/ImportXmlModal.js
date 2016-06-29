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
	
Ext.define('Desktop.ImportXmlModal',{
	extend: 'Ext.window.Window',
	layout:'fit',
        width:500,
        height:300,
        closable: true,
        resizable: true,
		modal:true,
		
        //plain: true,
		
        border: true,
		title:'Importation de fichier',
		
		
        items: [ {
				xtype:'form'
				
				//,height: 150
				,fileUpload: true
				,frame: false
				,url: '/UploadDocument'
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
					,acceptMimes: [ 'xml', 'zip'] // file types
					,acceptSize: 20000
					,fieldLabel: 'Fichier  <span class="gray">(xml, zip; 20 Mo max)</span>'
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
							signingUrl: '/UploadDocument',
							successCallback: function(response) {
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
	
	
/**
 * @class AmazonS3

Provides a utility class for uploading files direct to the Amazon S3 service, using CORS.
Requires a server-side page to be created, as specified by AmazonS3#signingUrl, which takes in the file name ("name") and file type ("type"), and digitally signs the request for submission to Amazon S3.


Class created by Daniel Gallo, based on non-Sencha code sample from [here](http://www.ioncannon.net/programming/1539/direct-browser-uploading-amazon-s3-cors-fileapi-xhr2-and-signed-puts/).


Example usage:

    AmazonS3.uploadFile(fileUploadField);

You can also pass in a configuration to override some of the default properties:

    AmazonS3.uploadFile(fileUploadField, {
        signingUrl: 'newurl.php',
        invalidFileMessage: 'Please select a file.'
    });

And have callbacks when a file has successfully uploaded:

    AmazonS3.uploadFile(fileUploadField, {
        successCallback: function(response) {
            console.log('success');
        }
    });

Preview:

{@img fileuploadfield.png File upload field in a form}

During upload:

{@img progressbar.png Progress bar during upload}

Example signing page (in PHP):

    <?php

    // The following 3 properties are specific to your Amazon S3 setup. The Secret Key should obviously not be shared or divulged.
    $S3_KEY='Public Key Here';
    $S3_SECRET='Secret Key Here';
    $S3_BUCKET='/Bucket Name Here';

    $EXPIRE_TIME=(60 * 5); // 5 minutes
    $S3_URL='http://s3-us-west-2.amazonaws.com';

    // The full file name including extension
    $objectName='/' . urlencode($_GET['name']);

    // File MIME type
    $mimeType=$_GET['type'];
    $expires = time() + $EXPIRE_TIME;
    $amzHeaders= "x-amz-acl:public-read";

    // The string to sign, based on the request type, MIME type of the file, headers and file path
    $stringToSign = "PUT\n\n$mimeType\n$expires\n$amzHeaders\n$S3_BUCKET$objectName";

    // Sign the string with the S3 Secret key.
    $sig = urlencode(base64_encode(hash_hmac('sha1', $stringToSign, $S3_SECRET, true)));

    // Generate the URL to where the file should be uploaded on Amazon S3, appending query string params such as the S3 public key, expiry time and signature
    $url = urlencode("$S3_URL$S3_BUCKET$objectName?AWSAccessKeyId=$S3_KEY&Expires=$expires&Signature=$sig");

    // Return the signed Amazon S3 URL
    echo $url;
    ?>

 * @singleton
 */
Ext.define('AmazonS3', {
    singleton: true,

    requires: [
        'Ext.Ajax',
        'Ext.ProgressBar',
        'Ext.String',
        'Ext.window.MessageBox',
        'Ext.window.Window'
    ],

    config: {
        /**
        * @cfg {Ext.form.field.File} fileUploadField
        * The file upload field containing the file to upload.
        */
        fileUploadField: null,

        /**
        * @cfg {Boolean} allowCancel
        * If set to true, the user will be able to cancel the upload through the use of a Cancel button.
        */
        allowCancel: true,

        /**
        * @cfg {String} cancelText
        * Text that's shown within the Cancel button.
        */
        cancelText: 'Cancel',

        /**
        * @cfg {String} signingUrl
        * The URL to your page that accepts the file name and file type, and returns a signed Amazon S3 URL for uploading to the S3 service.
        * You can see an [example of a PHP signing page here](http://www.ioncannon.net/programming/1539/direct-browser-uploading-amazon-s3-cors-fileapi-xhr2-and-signed-puts/).
        */
        signingUrl: null,

        /**
        * @cfg {String} invalidFileMessage
        * Message that's shown to the user if there isn't a file to upload.
        */
        invalidFileMessage: 'Please provide a file to upload.',

        /**
        * @cfg {String} invalidBrowserMessage
        * Message that's shown to the user if their browser doesn't support this type of file upload.
        */
        invalidBrowserMessage: 'Your browser doesn\'t support the ability to upload files using this method.',

        /**
        * @cfg {String} finalizingText
        * The text that's shown within the Progress Bar when the upload is finalising.
        */
        finalizingText: 'Finalising.',

        /**
        * @cfg {String} uploadingText
        * The text that's shown within the Progress Bar when the file is uploading.
        */
        uploadingText: 'Uploading.',
		processingText:'Processing',
        /**
        * @cfg {String} abortedText
        * The text that's shown within the Progress Bar when the upload has been aborted.
        */
        abortedText: 'Aborted.',

        /**
        * @cfg {String} completedText
        * The text that's shown within the Progress Bar when the upload has completed successfully.
        */
        completedText: 'Upload completed.',

        /**
        * @event progressCallback
        * Fired when the file commences upload and there is progress information.
        * @param {Object} progress The progress event object.
        */
        progressCallback: null,

        /**
        * @event successCallback
        * Fired when the file has successfully uploaded to the remote server.
        * @param {Object} response The response object.
        */
        successCallback: null,

        /**
        * @event failureCallback
        * Fired when the file has failed to upload to the remote server.
        */
        failureCallback: null,

        /**
        * @event abortCallback
        * Fired when the upload has been cancelled by the user.
        * @param {Object} response The response object.
        */
        abortCallback: null,

        // Reference to the progress bar component.
        progressBar: null,

        // Reference to the progress window component.
        progressWindow: null,

        // Reference to the XMLHttpRequest object.
        xhr: null
    },

    /**
    * Uploads the file from the provided {@link Ext.form.field.File} field.
    * @param {Ext.form.field.File} fileUploadField The file upload field.
    * @param {Object} config The configuration options.
    */
	currentFileIndex:0,
	withNoProcessing:false,
	fileName:null,
	uploadFile:function(fileUploadField, config) {
		var me = this;

        me.fileUploadField = fileUploadField;

        config = config || {};
        Ext.apply(me, config);

        // If there's no file selected in the file upload field
        if (me.fileUploadField.inputEl.dom.files.length === 0) {
            me.showError(me.getInvalidFileMessage());
            return;
        }
		var files = me.fileUploadField.inputEl.dom.files;
		me.currentFileIndex = 0;
		me.uploadFile2();
	},
	
    uploadFile2: function() {
        var me=this;
		
		if (me.currentFileIndex>=me.fileUploadField.inputEl.dom.files.length ) {
			
			Ext.callback(me.successCallback, me, []);
			return;
		}
        var file = me.fileUploadField.inputEl.dom.files[me.currentFileIndex];
		me.currentFileIndex=me.currentFileIndex+1;
        var url = me.signingUrl;

        
        me.startUpload(file,url);
        
    },

    /**
    * Cancels the upload currently in progress.
    * @param {Ext.button.Button} button Reference to the Cancel button.
    */
    cancelUpload: function(button) {
        var me = this;

        // Abort the file upload
        me.xhr.abort();

        // Stop the button being clicked multiple times
        button.disable();

        // Update the progress bar to zero percent, and show the aborted text
        me.updateStatus(0, me.abortedText);

        // Close progress window after one second
        me.closeProgressWindow();

        Ext.callback(me.abortCallback, me);
    },
	processing:function(currentUploadJobId) {
		var me=this;
		
		$.getJSON('/service/job',{action:'getProgress',jobid:currentUploadJobId}, function(projectInfo) {								
			
			// progress: -1, id: 1, state: "STARTED", error: null} 
			if (projectInfo.error != null) {
				alert(projectInfo.error);
				Ext.callback(me.failureCallback, me, [projectInfo.error]);					
				me.closeProgressWindow();
				
			} else {
				if (projectInfo.progress == 100) {					
					me.updateStatus(100, "traitement...100%");
					
					Ext.callback(me.uploadFile2, me, []);
					
					me.closeProgressWindow();
				} else {
					if (projectInfo.progress>0 ) {						
						me.updateStatus(projectInfo.progress, "traitement ..."+projectInfo.progress+"%");
					}
					//window.setTimeout("processing("+currentUploadJobId+")", 500);
					Ext.Function.defer(me.processing,1000,me,[currentUploadJobId]);
					/*
					(function (num) {
						me.processing(currentUploadJobId);
					}).defer(500);
					*/
				}
			}
		});
		
		
	},
	
    /**
    * Starts the file upload process.
    * @private
    * @param {Object} file The underlying File Object file from the {@link Ext.form.field.File} field.
    * @param {String} signedUrl The Amazon S3 signed url. This is the url to where the file will be uploaded, and should contain a signature for authorising the request.
    */
    startUpload: function(file, signedUrl) {
        var me = this,
            xhr = me.xhr = me.createCorsRequest('POST', signedUrl);
			

        if (!xhr) {
            me.showError(me.getInvalidBrowserMessage());
        } else {
            xhr.onload = function() {
                if (xhr.status == 200) {
                    //Ext.callback(me.processing, me, [xhr]);
					me.updateStatus(100, "chargement...100%");
					
					var d = $.parseJSON(xhr.response);
					if (d.error!=null) {
						alert(d.error);
					}
					if (me.withNoProcessing == true) {						
						me.closeProgressWindow();
						
						Ext.callback(me.successCallback, me, [xhr.response]);
						return; 
					}
					Ext.callback(me.processing, me, [d.id]);
					//me.processing(xhr.responseJSON.id);
					
                    

                    // Reset the file upload field after upload has successfully completed.
                    //me.fileUploadField.reset();

                    
                } else {
                    Ext.callback(me.failureCallback, me, [xhr]);

                    me.updateStatus(0, 'Upload error: ' + xhr.status);
                }

                me = null;
            };

            xhr.onerror = function(response) {
                Ext.callback(me.failureCallback, me, [response]);

                // Upload error - show message to user
                me.updateStatus(0, 'An upload error has occurred.');

                me = null;
            };

            xhr.upload.onprogress = function(e) {
                Ext.callback(me.progressCallback, me, [e]);

                if (e.lengthComputable) {
                    // File is uploading, get the progress of the upload and update the progress bar based on this information
                    var percentLoaded = Math.round((e.loaded / e.total) * 100);
					me.uploadingText="chargement..."+percentLoaded+"%";
                    me.updateStatus(percentLoaded, percentLoaded == 100 ? me.finalizingText : me.uploadingText);
                }
            };

            xhr.setRequestHeader('Content-Type', file.type);
			if (me.fileName==null) {
				xhr.setRequestHeader('File-Name', file.name);
			} else {
				xhr.setRequestHeader('File-Name',me.fileName);
			}
			//xhr.setRequestHeader('Content-Type', "multipart/form-data");
			 
            //xhr.setRequestHeader('x-amz-acl', 'public-read');

            xhr.send(file);
        }
    },

    /**
    * Shows an error message inside a {@link Ext.window.MessageBox}.
    * @private
    * @param {String} error The error message to show inside the generated {@link Ext.window.MessageBox}.
    */
    showError: function(error) {
        Ext.Msg.show({
            title: 'Error',
            msg: error,
            buttons: Ext.Msg.OK,
            icon: Ext.Msg.ERROR
        });
    },

    /**
    * Updates the status of the file upload using a progress bar.
    * @private
    * @param {Number} percent The percentage complete of the file upload.
    * @param {String} status The status message of the file upload.
    */
    updateStatus: function(percent, status) {
		//console.log(percent,status);
        var me = this;

        if (!me.progressWindow) {
            var allowCancel = me.allowCancel,
                windowHeight = (allowCancel ? 140 : 102),   // Alter the window's height, based on whether the toolbar and Cancel button are included
                dockedItems = {};

            if (allowCancel) {
                dockedItems = {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [{
                        xtype: 'tbfill'
                    }, {
                        xtype: 'button',
                        text: me.cancelText,
                        handler: function(button) {
                            me.cancelUpload(button);
                        }
                    }, {
                        xtype: 'tbfill'
                    }]
                };
            }

            // Create a progress window, containing a progress bar.
            me.progressWindow = Ext.create('Ext.window.Window', {
                title: 'File Upload',
                height: windowHeight,
                width: 350,
                bodyPadding: 20,
                modal: true,
                closable: false,
                draggable: false,
                resizable: false,
                layout: 'fit',
                items: [
                    {xtype: 'progressbar',labelField:"chargement"}
                ],
                dockedItems: dockedItems
            }).show();

            me.progressBar = me.progressWindow.down('progressbar');
        }

        // Update the progress bar based on percentage complete, and show the associated status message.
        //me.getProgressBar().updateProgress(percent / 100, status);
		me.progressBar.updateProgress(percent / 100, status);
    },

    /**
    * Closes the progress window. Called either when the upload has completed or the user has cancelled the upload.
    * @private
    */
    closeProgressWindow: function() {
        var me = this;

        // Destroy the progress window after one second following the completion of the file upload.
        Ext.Function.defer(function(){
            //Ext.destroy(me.getProgressWindow());
			Ext.destroy(me.progressWindow);
            me.progressWindow = null;
            me.progressBar = null;
        }, 1000);
    },

    /**
    * Creates a CORS request that can then be used for the file upload.
    * @private
    * @param {String} method The method to use in the request (PUT, POST).
    * @param {String} url The Amazon S3 url where the file should be uploaded, as returned from the signing url.
    * @return {Object}
    */
    createCorsRequest: function(method, url) {
        var xhr = new XMLHttpRequest();

        if ("withCredentials" in xhr) {
            xhr.open(method, url, true);
        } else if (typeof XDomainRequest != "undefined") {
            xhr = new XDomainRequest();
            xhr.open(method, url);
        } else {
            xhr = null;
        }

        return xhr;
    }
});	