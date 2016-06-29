/**
 * Ext JS Library
 * Copyright(c) 2006-2014 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 * @class Ext.ux.desktop.App
 */
Ext.define('Ext.ux.desktop.App', {
    mixins: {
        observable: 'Ext.util.Observable'
    },

    requires: [
        'Ext.container.Viewport',

        'Ext.ux.desktop.Desktop'
    ],

    isReady: false,
    modules: null,
    useQuickTips: true,

    constructor: function (config) {
        var me = this;

        me.mixins.observable.constructor.call(this, config);

        if (Ext.isReady) {
            Ext.Function.defer(me.init, 10, me);
        } else {
            Ext.onReady(me.init, me);
        }
    },

    init: function() {
        var me = this, desktopCfg;

        if (me.useQuickTips) {
            Ext.QuickTips.init();
        }

        me.modules = me.getModules();
        if (me.modules) {
            me.initModules(me.modules);
        }

        desktopCfg = me.getDesktopConfig();
        me.desktop = new Ext.ux.desktop.Desktop(desktopCfg);

        me.viewport = new Ext.container.Viewport({
            layout: 'fit',
            items: [ me.desktop ]
        });

        Ext.getWin().on('beforeunload', me.onUnload, me);

        me.isReady = true;
        me.fireEvent('ready', me);
    },

    /**
     * This method returns the configuration object for the Desktop object. A derived
     * class can override this method, call the base version to build the config and
     * then modify the returned object before returning it.
     */
    getDesktopConfig: function () {
        var me = this, cfg = {
            app: me,
            taskbarConfig: me.getTaskbarConfig()
        };

        Ext.apply(cfg, me.desktopConfig);
        return cfg;
    },

    getModules: Ext.emptyFn,

    /**
     * This method returns the configuration object for the Start Button. A derived
     * class can override this method, call the base version to build the config and
     * then modify the returned object before returning it.
     */
    submenu:[
             {text: 'Schéma', id:"schema",nomenu:true},
             {text: 'Tableaux', id:"tableau",menu:[]},
             {text: 'Tris simples', id:"tri-simple",menu:[]},
             {text: 'Filtres', id:"filtre",menu:[]},  
             {text: 'Historique', id:"historique",nomenu:true},
             {text: 'Etiquetage', id:"etiquetage",nomenu:true},
             {text: 'Codage', id:"codage",nomenu:true},
             {text: 'Administration', id:"admin",menu:[]}
             //{text: 'Autre', id:"default",menu:[]}
             
    ],
    getSubmenu:function(submenu_id) {
    	
    	for (var i=0;i<this.submenu.length;i++) {
    		if (this.submenu[i].id == submenu_id) {
    			
    			return i;
    			/*
    			if (this.submenu[i].nomenu) {
    				return i
    			} else {
    				return this.submenu[i].menu;
    			}
    			*/
    		}
    	}
    	
    	return null;
    },
    getStartConfig: function () {
        var me = this,
            cfg = {
                app: me,
                menu: []
            },
            launcher;

        Ext.apply(cfg, me.startConfig);
        var submenu = [];
        Ext.each(me.modules, function (module) {
            launcher = module.launcher;
            if (launcher) {
            	
                launcher.handler = launcher.handler || Ext.bind(me.createWindow, me, [module]);
                
                
                
                var idmenu = "default";
                if (launcher.hasOwnProperty('idmenu')) {
                	idmenu = launcher.idmenu;
                }
               
                var submenu_index = me.getSubmenu(idmenu); // todo : prendre le code sub menu dans module.submenu
                
                if (me.submenu[submenu_index].nomenu) {
                	me.submenu[submenu_index]=module.launcher;
                } else {
                	me.submenu[submenu_index].menu.push(module.launcher);
                }
            }
        });
        
        var admin_index = me.getSubmenu("admin");
        var hasAdmin = this.submenu[admin_index].menu.length>0;
        for (var i=0;i<this.submenu.length;i++) {
        	if (hasAdmin && admin_index==i) { 
        		cfg.menu.push(this.submenu[i]);
        	}
        	if (!hasAdmin && admin_index!=i) {
        		cfg.menu.push(this.submenu[i]);
        	}
        }
        return cfg;
    },

    createWindow: function(module) {
    	var startmenu = Ext.getCmp("startmenu");
    	//console.log("startmenu=",startmenu.getId());
    	startmenu.deactivateActiveItem();
        var window = module.createWindow();
        window.show();
        return true;
    },

    /**
     * This method returns the configuration object for the TaskBar. A derived class
     * can override this method, call the base version to build the config and then
     * modify the returned object before returning it.
     */
    getTaskbarConfig: function () {
        var me = this, cfg = {
            app: me,
            startConfig: me.getStartConfig()
        };

        Ext.apply(cfg, me.taskbarConfig);
        return cfg;
    },

    initModules : function(modules) {
        var me = this;
        Ext.each(modules, function (module) {
            module.app = me;
        });
    },

    getModule : function(name) {
    	var ms = this.modules;
        for (var i = 0, len = ms.length; i < len; i++) {
            var m = ms[i];
            if (m.id == name || m.appType == name) {
                return m;
            }
        }
        return null;
    },

    onReady : function(fn, scope) {
        if (this.isReady) {
            fn.call(scope, this);
        } else {
            this.on({
                ready: fn,
                scope: scope,
                single: true
            });
        }
    },

    getDesktop : function() {
        return this.desktop;
    },

    onUnload : function(e) {
        if (this.fireEvent('beforeunload', this) === false) {
            e.stopEvent();
        }
    }
});
