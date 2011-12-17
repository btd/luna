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
