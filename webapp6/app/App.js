/*!
 * Ext JS Library
 * Copyright(c) 2006-2014 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */


Ext.define('HistoModel',{ // was book
        extend: 'Ext.data.Model',
		
        fields: [                 
            'type', 'from','timestamp'
			]
        
    });

Ext.define('HistoStore', {	
	extend: 'Ext.data.Store',
    model: 'HistoModel'    
});

Ext.define('Desktop.App', {
    extend: 'Ext.ux.desktop.App',
	
    requires: [
        'Ext.window.MessageBox',
		'Ext.ux.form.ItemSelector',
        //'Desktop.GridWindow',
		'Desktop.ImportXmlModal',
		'Desktop.ImportProjetModal',
		'Desktop.ImportCodage',
		 'Desktop.ProjectModal',
		 'Desktop.SchemaWindow',
		 //'Desktop.ModalitesWindow',
		 'Desktop.TableauBrutWindow',
		 //'Desktop.CodageWindow',
		  'Desktop.DownloadCodage',
		  'Desktop.EditCodage',
		  'Desktop.CodageWindow2',
		 'Desktop.ContingenceWindow',
		 'Desktop.EditorWindow',
		 'Desktop.FiltreWindow2'
		 ,		 'Desktop.EditFiltre2'
		 ,'Desktop.ExportFiltre'
		 ,'Desktop.ImportFiltre'
		 ,'Desktop.FullTextWindow'
		 ,'Desktop.ProjectServerModal'
		 ,'Desktop.EtiquetageWindow'
		 ,'Desktop.XQueryEditorWindow'
		 ,'Desktop.HistoWindow'
		 ,'Desktop.SavedQueryWindow'
		 ,'Desktop.CorrectionsWindow'
		 ,'Desktop.ExportTextometrieModal'
		 ,'Desktop.ModalitesWindowJson'
		 ,'Desktop.ImportSchemaWindow'
		 ,'Desktop.GenererSchemaWindow'
        //,'Desktop.LoginWindow',
        //,'Desktop.AdminDBManager'
    ],
    user:null,
    
    init: function() {
        // custom logic before getXYZ methods get called...

        this.callParent();
        var win = null;
        var app=this;
        
        //  afficher un autre ProjectModal pour les user loggé > 2
		if (this.user!=null && this.user.id>2) {
			if (this.user.default_project!=null && this.user.default_project!=-1) {
				
				$.getJSON('/service/commands',{cmd:'openProject',id:this.user.default_project}, function(project) {	
					Ext.getCmp("startWindow").setTitle("--");
									
									
					var module = app.getModule('schema-win');
					var schemaWindow = module.createWindow();												
					schemaWindow.x = 0;
					schemaWindow.y = 0;						
					schemaWindow.show();						
				});
			} else {
				win = Ext.create('Desktop.ProjectServerModal');
			}
		}	else {	
			
			win = Ext.create('Desktop.ProjectModal');
			
		}
		
		if (win!=null) {
			win.modal = true;
			win.app = this;			
			win.show();
		}
    },
    isAdmin:false,
    histoStore:Ext.create('HistoStore'),
    addToHisto:function(req) { 
    	req.timestamp=new Date();
    	this.histoStore.insert(0, [req]);
    	
    	//histo.push(req); // req : {from:"modalite",type:"query",params:{},timestamp:null}
    },
    getModules : function(){
    	/*
    	var readwrite = false;
        if (this.user!=null && this.user.credential!=null) {
        	readwrite = this.user.credential.readwrite;
        }
        if (this.user == null) {
        	readwrite = true;
        }
        */
    	var readwrite = true;
    	if (this.user!=null) {
    		readwrite = this.user.credential.readwrite;
    	} 
    	
        var mods=
        [
            
           // new Desktop.GridWindow(),
			new Desktop.SchemaWindow(),
			new Desktop.GenererSchemaWindow(),
			new Desktop.ImportSchemaWindow(),
			
			//new Desktop.ModalitesWindow(),
			new Desktop.TableauBrutWindow(),
			//new Desktop.CodageWindow(),
			new Desktop.CodageWindow2(),
			new Desktop.ContingenceWindow(),
			new Desktop.FiltreWindow2(),
			new Desktop.FullTextWindow(),
			
			new Desktop.ModalitesWindowJson(),
			new Desktop.SavedQueryWindow()
			
			
			//,new Desktop.EditorWindow()
           
        ];
        //console.log("readwrite=",readwrite);
        //alert(readwrite);
        if (readwrite == true) {
        	mods.push(new Desktop.EtiquetageWindow());
        	mods.push(new Desktop.HistoWindow());
        	mods.push(new Desktop.CorrectionsWindow());
        } else {
        	
        	
        }
        return mods;
    },

    getDesktopConfig: function () {
        var me = this, ret = me.callParent();

        return Ext.apply(ret, {
            //cls: 'ux-desktop-black',
/*
            contextMenuItems: [
                { text: 'Change Settings', handler: me.onSettings, scope: me }
            ],
	*/		
            wallpaper: 'resources/images/wallpapers/desk.jpg',
            wallpaperStretch: false
        });
    },

    // config for the start menu
    
    getStartConfig : function() {
    	/*
    	return Ext.create('Ext.Button', {
    	    text: 'Click me',
    	      
    	    handler: function() {
    	        alert('You clicked the button!')
    	    }
    	});
    	 */
    	
    	console.log("getStartConfig");
        var me = this, ret = me.callParent();
        var readwrite = false;
        if (this.user!=null && this.user.credential!=null) {
        	readwrite = this.user.credential.readwrite;
        }
        if (this.user == null) {
        	readwrite = true;
        }
        if ( readwrite==true ) { 
	        return Ext.apply(ret, {
	            title: 'ClioXML',
				id:'startWindow',
	            iconCls: 'user',
	            height: 300,
	            toolConfig: {
	                width: 150,
	                items: [
						{
	                        text:'Importer du XML',
	                        iconCls:'settings',
	                        handler: me.onImport,                        
	                        scope: me
	                    },					
	                    '-',
	                    {
	                        text:'Fermer',
	                        iconCls:'settings',
	                        handler: me.onCloseProject,
	                        scope: me
	                    }
						
	                ]
	            }	        
	        });
        } else {
        	return Ext.apply(ret, {
	            title: 'ClioXML',
				id:'startWindow',
	            iconCls: 'user',
	            height: 300,
	            toolConfig: {
	                width: 100,
	                items: [
						
	                    {
	                        text:'Fermer',
	                        iconCls:'settings',
	                        handler: me.onCloseProject,
	                        scope: me
	                    }
						
	                ]
	            }	        
	        });
        }
    },

    getTaskbarConfig: function () {
        var ret = this.callParent();

        return Ext.apply(ret, {
		/*
            quickStart: [                
                //{ name: 'Grid Window', iconCls: 'icon-grid', module: 'grid-win' },
				{ name: 'Schema Window', iconCls: 'icon-grid', module: 'schema-win' },
				{ name: 'Modalités Window', iconCls: 'icon-grid', module: 'modalites-win' }
            ],
			*/
			alwaysOnTop:true,
            trayItems: [
                { html:'ClioXML'}
            	
            ] // trayItems
        });
    },

    onLogout: function () {
        Ext.Msg.confirm('Logout', 'Are you sure you want to logout?');
    },
	onImport: function () {
        var win = Ext.create('Desktop.ImportXmlModal');		
		win.app = this;
		win.show();
    },
    onCloseProject: function () {
    	var msg="";
    	if (this.user.credential.readwrite==false) {
    		msg="\n Vous allez perdre des données !";
    	}
        Ext.MessageBox.show({
				title: 'Fermeture du projet',
				msg: 'Confirmez vous la fermeture du projet ?'+msg,
				buttons: Ext.MessageBox.OKCANCEL,
				icon: Ext.MessageBox.WARNING,
				fn: function(btn){
					if(btn == 'ok'){
						location.reload(true);
					} else {
						return;
					}
				}
			});
    }
});
