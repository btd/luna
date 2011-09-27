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
        ul.siblings("input").val(a.attr("href"));
        return false;
    });
    if ($("form.sub_menu").children("input").length != 0) {
        var dialog_add_repo = $("form.sub_menu").dialog({ autoOpen: false, modal: true, width: "600px" });

        $("#sub_menu_holder").append("<button class='button'>Add repository</button>");
        $("#sub_menu_holder > button").click(function() {
            dialog_add_repo.dialog("open");
            $("#create_repo_button").click(function() {
                dialog_add_repo.dialog("close");
                return true;
            })
            return false;
        });
    }
});











