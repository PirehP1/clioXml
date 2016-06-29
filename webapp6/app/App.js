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
		 'Desktop.ModalitesWindow',
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
		 ,'Desktop.ExportTextometrieModal'
        //,'Desktop.LoginWindow',
        //,'Desktop.AdminDBManager'
    ],
    user:null,
    
    init: function() {
        // custom logic before getXYZ methods get called...

        this.callParent();
        var win = null;
        //  afficher un autre ProjectModal pour les user loggé > 2
		if (this.user!=null && this.user.id>2) {
			win = Ext.create('Desktop.ProjectServerModal');
		}	else {	
			win = Ext.create('Desktop.ProjectModal');
			
		}
		
		win.modal = true;
		win.app = this;
		
		win.show();
    },
    isAdmin:false,
    histoStore:Ext.create('HistoStore'),
    addToHisto:function(req) { 
    	req.timestamp=new Date();
    	this.histoStore.insert(0, [req]);
    	
    	//histo.push(req); // req : {from:"modalite",type:"query",params:{},timestamp:null}
    },
    getModules : function(){
    	var readwrite = false;
        if (this.user!=null && this.user.credential!=null) {
        	readwrite = this.user.credential.readwrite;
        }
        if (this.user == null) {
        	readwrite = true;
        }
        var mods=
        [
            
           // new Desktop.GridWindow(),
			new Desktop.SchemaWindow(),
			new Desktop.ModalitesWindow(),
			new Desktop.TableauBrutWindow(),
			//new Desktop.CodageWindow(),
			new Desktop.CodageWindow2(),
			new Desktop.ContingenceWindow(),
			new Desktop.FiltreWindow2(),
			new Desktop.FullTextWindow(),
			new Desktop.HistoWindow()
			
			
			//,new Desktop.EditorWindow()
           
        ];
        
        if (readwrite) {
        	mods.push(new Desktop.EtiquetageWindow());
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
	                width: 100,
	                items: [
						{
	                        text:'Import',
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
        Ext.MessageBox.show({
				title: 'Fermeture du projet',
				msg: 'Confirmez vous la fermeture du projet ',
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
