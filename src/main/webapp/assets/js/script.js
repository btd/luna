/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

/* Author:
 Bardadym Denis
 */
$(function() {
    if (!Modernizr.input.placeholder) {

        $('[placeholder]').focus(
            function() {
                var input = $(this);
                if (input.val() == input.attr('placeholder')) {
                    input.val('');
                    input.removeClass('placeholder');
                }
            }).blur(
            function() {
                var input = $(this);
                if (input.val() == '' || input.val() == input.attr('placeholder')) {
                    input.addClass('placeholder');
                    input.val(input.attr('placeholder'));
                }
            }).blur();
        $('[placeholder]').parents('form').submit(function() {
            $(this).find('[placeholder]').each(function() {
                var input = $(this);
                if (input.val() == input.attr('placeholder')) {
                    input.val('');
                }
            })
        });
    }
    

    $(".url-box > .clone-urls > li > a").click(function() {
        var a = $(this);
        var ul = a.parent().parent();
        ul.children().removeClass("selected");
        a.parent().addClass("selected");
        ul.siblings("input").val(a.attr("href"));
        return false;
    });

    $(".url-box > .clone-urls > li:first-child > a").click();

    $("form.replaceDialog").each(function(i, form){
        var el = $(form);
        var newLink = ""
        if(el.attr('data-button-text') !== undefined)
            newLink = "<button class='button'>" + el.attr('data-button-text') + "</button>"
        else 
            newLink = "<a href='javascript://'>" + el.attr('data-anchor-text') + "</a>"
        var newButton = $(newLink).click(function(){
            el.dialog("open");
            $('button', el).click(function(){
                el.dialog.dialog("close");
                return true;
            });
            return false;
        });
        el.parent().append(newButton)
        el.dialog({ autoOpen: false, modal: true, width: "600px" });        
    });

    $(".selectmenu").selectmenu();
    

    $(".blob").each(function(i, el) {
        var code = $("pre code", el);
        var linesCount = code.text().split("\n").length;
        var rawLN = "";
        //code.empty();
        for(var i = 0; i < linesCount; i++){
            rawLN += "<span class='line_number'>"+ i +"</span>\n";
        }
        hljs.highlightBlock(code[0]);
        $(el).append("<div class='source_container'><div class='line_numbers'><pre >"+rawLN+"</pre></div><div class='source'></div></div");
        $(".source", el).append($("pre", el)[0]);
        $(".source_container", el).css("padding-left", $(".line_numbers", el).width());
    });

    CodeScrolling.initFloatingScrollbars($(window));
    DiffHeader.init();
    //CodeScrolling.init();
    
});












