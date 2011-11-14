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
    $(".url-box > .clone-urls > li:first-child").toggleClass("selected");
    $(".url-box > .clone-urls > li > a").click(function() {
        var a = $(this);
        var ul = a.parent().parent();
        ul.children().toggleClass("selected")
        ul.siblings("input").val(a.attr("href"));
        return false;
    });

    $("form.replaceDialog").each(function(i, form){
        var el = $(form);
        var newButton = $("<button class='button'>" + el.attr('data-button-text') + "</button>").click(function(){
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

    if (hljs) { hljs.initHighlightingOnLoad(); }
});












