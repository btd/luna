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

     var dialog_add_key= $("form.add_key_form").dialog({ autoOpen: false, modal: true, width: "600px" });

        $("#add_key_holder").append("<button class='button'>Add key</button>");
        $("#add_key_holder > button").click(function() {
            dialog_add_key.dialog("open");
            $("#add_key_button").click(function() {
                dialog_add_key.dialog("close");
                return true;
            })
            return false;
        });

    var dialog_add_collaborator= $("form.add_collaborator_form").dialog({ autoOpen: false, modal: true, width: "600px" });

        $("#add_collaborator_holder").append("<button class='button'>Add collaborator</button>");
        $("#add_collaborator_holder > button").click(function() {
            dialog_add_collaborator.dialog("open");
            $("#add_collaborator_holder").click(function() {
                dialog_add_collaborator.dialog("close");
                return true;
            })
            return false;
        });

    $(".selectmenu").selectmenu();

    if (hljs) { hljs.initHighlightingOnLoad(); }
});












