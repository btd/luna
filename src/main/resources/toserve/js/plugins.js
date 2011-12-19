window.fDomLoaded=false;
document.head=document.getElementsByTagName("head")[0];
if(document.all&&!document.getElementById){
    document.getParent=function(A){
        return A.parentElement
    }
}else{
    document.getParent=function(A){
        return A.parentNode
    }
}
function detectBrowser(){
    window.ie=(document.all&&!window.opera);
    if(navigator&&navigator.userAgent){
        var A=navigator.userAgent;
        window.webkit=(A.indexOf("WebKit/")!=-1);
        if(window.webkit){
            window.versionMajorWebKit=0;
            window.versionMinorWebKit=0;
            var C=/WebKit\/(\d)(\d\d)?/;
            var B=C.exec(A);
            if(B){
                window.versionMajorWebKit=parseInt(B[1]);
                if(B[2]){
                    window.versionMinorWebKit=parseInt(B[2])
                }
            }
            window.chrome=(A.indexOf("Chrome/")!=-1);
            window.safari=!window.chrome&&(A.indexOf("Safari/")!=-1);
            window.webkitPre31=window.versionMajorWebKit<5||(window.versionMajorWebKit==5&&window.versionMinorWebKit<25);
            window.webkitMobile=/ Mobile\//.test(A)
        }
        if(window.ie){
            window.ie9=(A.indexOf("MSIE 9")!=-1);
            window.ie8=(A.indexOf("MSIE 8")!=-1);
            window.ie7=(A.indexOf("MSIE 7")!=-1);
            window.ie6=(window.ie&&!window.ie7&&!window.ie8&&!window.ie9);
            window.legacyie=(window.ie&&(window.ie6||window.ie7||window.ie8))
        }
        window.mozilla=(!window.ie&&!window.webkit&&!window.opera&&(A.indexOf("Mozilla")!=-1));
        window.ff3=(window.mozilla&&A.indexOf("Firefox/3")!=-1);
        window.macintosh=(A.indexOf("Macintosh")!=-1)
    }
}
detectBrowser();
(function(){
    var A=window.Util={
        nScrollerWidth:null,
        scrollerWidth:function(){
            if(null===A.nScrollerWidth){
                var D=$('<div id="scrollerOuter"></div>').css({
                    width:100,
                    height:100,
                    position:"absolute",
                    left:0,
                    top:0,
                    overflow:"scroll"
                });
                var B=$('<div id="scrollerInner"></div>').css({
                    height:50
                });
                D.append(B);
                $("body").append(D);
                var E=B.width();
                D.css("overflow","hidden");
                var C=Math.max(B.width(),D.width());
                A.nScrollerWidth=C-E;
                D.remove();
            }
            return A.nScrollerWidth
        }
        
    }
})();

(function(){
    var B=window.CodeScrolling={
        init:function(){
            $(".blob").each(B.initForTable)
        },
        initForTable:function(C){
            $(".source",C).unbind("scroll.CodeScrolling").bind("scroll.CodeScrolling",function(){
                var F=$(this).find("pre, .jFloatingScroller");
                var E=$(this).scrollLeft();
                var D=$(this);
                F.not(this).each(function(){
                    if($(this).data("CodeScrolling.skips")){
                        $(this).data("CodeScrolling.skips",false);
                        return
                    }
                    D.data("CodeScrolling.skips",true);
                    $(this).scrollLeft(E)
                })
            })
        },
        jFloatingScrollers:null,
        _createFloatingScrollers:function(){
            if(B.jFloatingScrollers&&B.jFloatingScrollers.length){
                return
            }
            B.jFloatingScrollers=$("<div />").addClass("jFloatingScroller").height(window.mozilla?Util.scrollerWidth():"auto").append($("<pre />").html("&nbsp;"));
            B.jFloatingScrollers=B.jFloatingScrollers.add(B.jFloatingScrollers.clone())
        },
        initFloatingScrollbars:function(G){
            if(window.ie){
                return
            }
            B._createFloatingScrollers();
            G=G||$(window);
            var C=null;
            var F=function(H){
                B.jFloatingScrollers.hide();
                if(!H){
                    return
                }
                $(".source",H).each(function(K){
                    var I=$(this);
                    var J=B.jFloatingScrollers.eq(K).show().width($(this).width()).css("left",$(this).offset().left).unbind("scroll").scroll(function(){
                        if($(this).data("CodeScrolling.skips")){
                            $(this).data("CodeScrolling.skips",false);
                            return
                        }
                        B.jFloatingScrollers.data("CodeScrolling.skips",true);
                        I.data("CodeScrolling.skips",true).scrollLeft($(this).scrollLeft())
                    });
                    I.append(J);
                    J.find("pre").width($("pre",this).outerWidth()+parseInt(J.css("padding-left")));
                    J.scrollLeft(I.scrollLeft())
                })
            };
                
            var D=function(){
                var L=(G.offset()||$("body").offset()).top;
                var K=(G[0]==window?G.scrollTop():0);
                var J=G.height();
                var I=L+K+J;
                var H=function(N){
                    var O=$(".source",N),P=O.offset().top,M=O.outerHeight();
                    if(P<I&&(P+M>I)){
                        F(N);
                        return true
                    }
                    return false
                };
                    
                if(!C||!H(C)){
                    F(null);
                    $(".blob .source").css("height","auto");
                    $(".blob").each(function(){
                        if(H(this)){
                            C=this;
                            return false
                        }
                    })
                }
            };

            var debounce = function(func, wait) {
                var timeout;
                return function() {
                  var context = this, args = arguments;
                  var later = function() {
                    timeout = null;
                    func.apply(context, args);
                  };
                  clearTimeout(timeout);
                  timeout = setTimeout(later, wait);
                };
              };

            var throttle = function(func, wait) {
                var context, args, timeout, throttling, more;
                var whenDone = debounce(function(){ more = throttling = false; }, wait);
                return function() {
                  context = this; args = arguments;
                  var later = function() {
                    timeout = null;
                    if (more) func.apply(context, args);
                    whenDone();
                  };
                  if (!timeout) timeout = setTimeout(later, wait);
                  if (throttling) {
                    more = true;
                  } else {
                    func.apply(context, args);
                  }
                  whenDone();
                  throttling = true;
                };
              };
            
              
        
            var E=throttle(D,100);
            G.unbind(".Floating").bind("scroll.Floating resize.Floating",E);
            $(window).unbind(".Floating").bind("scroll.Floating resize.Floating",E);
            $(window).load(function(){
                setTimeout(E,50)
            });
            E()
        }
    };

})();
(function(){
    var C=window.DiffHeader={
        sSavedHeader:null,
        rgnDiffPositions:[],
        init:function(){
            if(0==$("#floatingChangesetDiffHeader").length){
                return
            }
            $("#floatingChangesetDiffHeader").hide();
            $(window).scroll(function(){
                if(C.updateHeader()){
            // DiffUtil.removeHighlight()
            }
            });
            $(window).load(C.cacheDiffPositions);
            $(window).resize(function(){
                C.cacheDiffPositions();
                C.updateHeader(true)
            });
            C.window=window.webkit?$("body"):$("html")
        },
        cacheDiffPositions:function(){
            var J=$(".blob");
            var I=J.length;
            for(var H=0;H<I;H++){
                C.rgnDiffPositions[H]=J.eq(H).offset().top
            }
        },
        updateHeader:function(H){
            var O=$(".blob");
            var L=O.length;
            if(L==0){
                return;
            }
            var K=C.window.scrollTop()+(window.ie?0:C.window.offset().top);
            if(C.rgnDiffPositions[0]>K){
                $("#floatingChangesetDiffHeader").hide();
                return true;
            }
            for(var J=0;J<L;J++){
                if(C.rgnDiffPositions[J]<K&&(J+1==L||C.rgnDiffPositions[J+1]>K+40)){
                    $("#floatingChangesetDiffHeader").show();
                    var N=O.eq(J).find(".blob_header");
                    if(N.attr("id")!=C.sSavedHeader||H){
                        C.sSavedHeader=N.attr("id");
                        var M=N.clone(true);
                        M.attr("path",M.attr("id"));
                        M.attr("id","");
                        var topMenu = $(".top_menu");
                        $("#floatingChangesetDiffHeader").css({
                            top: topMenu.height() + parseInt(topMenu.css("padding-top")) + parseInt(topMenu.css("padding-bottom")),
                            left: N.offset().left,
                            width: N.width() + parseInt(N.css("padding-left")) + parseInt(N.css("padding-right"))
                        });
                        $("#floatingChangesetDiffHeader .blob_header").replaceWith(M).css({
                            width:"auto"
                        });
                        return true;
                    }
                    return;
                }
                if(C.rgnDiffPositions[J]>K+100){
                    break
                }
            }
            var I=$("#floatingChangesetDiffHeader").is(":visible");
            $("#floatingChangesetDiffHeader").hide();
            return I;
        }
    };
})();
// usage: log('inside coolFunc', this, arguments);
window.log = function(){
  log.history = log.history || [];   // store logs to an array for reference
  log.history.push(arguments);
  if(this.console) {
      arguments.callee = arguments.callee.caller;
      console.log( Array.prototype.slice.call(arguments) );
  }
};
(function(b){function c(){}for(var d="assert,count,debug,dir,dirxml,error,exception,group,groupCollapsed,groupEnd,info,log,markTimeline,profile,profileEnd,time,timeEnd,trace,warn".split(","),a;a=d.pop();)b[a]=b[a]||c})(window.console=window.console||{});
