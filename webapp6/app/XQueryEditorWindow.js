
Ext.define('Desktop.XQueryEditorWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'etiquetage-win',

    init : function(){
        this.launcher = {
            text: 'Requête XQuery',
            iconCls:'icon-grid'
        };
    },

    createWindow : function(xquery){
		
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
              
                title:'Requête XQuery',
				y:0,
				/*
				x:400,
                width:740,
                height:$(window).height()-48,
				*/
				x:$(window).width()*0.3,
                width:$(window).width()*0.4,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'border',
				current_filtreId:-1,		
				items:[
					{
						xtype:'panel',
						collapsible: true,
						split: true,
						region:'north',
						itemId:'etiquetage_panel',
						height:300,
						html:'<div style="height:100%;height:100%" name="editor">',
						aceEditor:null,
						listeners:{
							afterrender:function() {
								
								//innerCt																
								//console.log("le svg  :",$("#"+this.id).find("svg"));
								$("#"+this.id).find("div[name='editor']").attr("id",this.id+"-editor");
								
								var langTools = ace.require("ace/ext/language_tools");
								editor = ace.edit(this.id+"-editor");
								this.up("window").editor = editor;
								/*
								editor.setOptions({
									enableBasicAutocompletion: true,
									enableSnippets: true,
									enableLiveAutocompletion: false
								});
								*/
								// ace editor semble etre 1.1.8, voir autre mode dans git ace-builds/scr
								editor.setTheme("ace/theme/twilight");
								//var Mode = require('ace/mode/xml').Mode;
								var Range = require('ace/range').Range;
								//var XMLMode = new Mode();
								editor.getSession().setMode('ace/mode/xquery');
								
								this.aceEditor = editor;
								editor.setValue(xquery);
								
								editor.on("change", function() {
									editor.getSession().clearAnnotations();
									var old_m = editor.getSession().getMarkers();
									
									for (var x in old_m) {
										
										if (old_m[x].clazz.indexOf('ace_xquery_') == 0){
											
											editor.getSession().removeMarker(x);
										}
									}
									
									
									XQLint = document.XQLint;
									
									var linter = new XQLint(editor.getValue(), { styleCheck: false, fileName: "toto" });
									
									
									
									var m = linter.getMarkers();            // warning + errors
									var annotations=[];
									
									for (var i=0;i<m.length;i++) {
										var level = m[i].level;
										
									
										annotations.push({
											column:m[i].pos.sc,
											row: m[i].pos.sl,
											text: m[i].message,
											type: level // also warning and information, error
											 });
										var range = new Range(m[i].pos.sl, m[i].pos.sc, m[i].pos.el, m[i].pos.ec);
										editor.getSession().addMarker(
											    range, "ace_xquery_"+level, "text"
											 );
									}
									editor.getSession().setAnnotations(annotations);
									
								});
								//initEditor(editor);
								var w = this.up("window");
								//this.aceEditor.getSession().on("change",w.onEditorChange.bind(w));
								
								
								
							},
							resize: function(){
								var graph_id = this.id+"-editor";
								this.aceEditor.resize(true);
								//resizeSVG(graph_id);
							}
						}
					},
					{
						region:'center',
						itemId:'result',
						xtype: 'textarea'
						/*
						itemId:'fulltext_panel',
						html:'<div style="height:100%;width:100%" name="editor2">',
						aceEditor:null,
						listeners:{
							afterrender:function() {								
								$("#"+this.id).find("div[name='editor2']").attr("id",this.id+"-editor2");
								
								var langTools = ace.require("ace/ext/language_tools");
								editor = ace.edit(this.id+"-editor2");
								this.up("window").editor2 = editor;
								
								editor.setOptions({
									enableBasicAutocompletion: false,
									enableSnippets: false,
									enableLiveAutocompletion: false
								});
								editor.setReadOnly(true);  
								editor.setTheme("ace/theme/twilight");
								var Mode = require('ace/mode/xml').Mode;
								var themode = new Mode();
								editor.getSession().setMode(themode);
								
								this.aceEditor = editor;
								//initEditor(editor);
								
								
							},
							resize: function(){
								var graph_id = this.id+"-editor2";
								this.aceEditor.resize(true);
								
							}
						} 
						*/
					} // panel 2 center
				], // items
				
				
				
				
				currentViewMode:'xml',
				
				
				listeners: {
					afterrender:function() {
						
						
					}
				},
				
				
				
				
				dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
									
									
									{																			
										xtype: 'button',
										text:"executer",
										//disabled:true,
										
										listeners: {
												click:function(button) {
													//this.up("window").saveDoc();
													var v = this.up("window").editor.getValue();
													var ta = this.up("window").down("textarea");
													ta.setRawValue("");
													ta.setLoading(true);
													//console.log(v);
													$.post('/service/commands', {cmd:'executeRawXQuery',xquery:v}, function(data, textStatus) {
														if (data.erreur!=null) {
															ta.setRawValue(data.erreur);
															
														} else {
															ta.setRawValue(data.result);
															
														}
														ta.setLoading(false);
													});
												}
											}
									},
										
									]
								}
								
								]
								
               
            });
        
        return win;
    }
});
