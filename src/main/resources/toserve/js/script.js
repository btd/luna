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
        ul.children().toggleClass("selected")
        ul.siblings("input.textfield").val(a.attr("href"));
        return false;
    });
})











