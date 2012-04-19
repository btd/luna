/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

/* Author:
 Bardadym Denis
 */
function setCloneUrls() {
    $(".url-box > .clone-urls > li > a").click(function() {
        var a = $(this);
        var ul = a.parent().parent();
        ul.children().removeClass("selected");
        a.parent().addClass("selected");
        ul.siblings("input").val(a.attr("href"));
        return false;
    });

    $(".url-box > .clone-urls > li:first-child > a").click();
}

function numberLines(node, isPreformatted) {
    var nocode = /(?:^|\s)nocode(?:\s|$)/;
    var lineBreak = /\r\n?|\n/;
  
    var document = node.ownerDocument;
  
    var li = document.createElement('div');
    while (node.firstChild) {
      li.appendChild(node.firstChild);
    }
    // An array of lines.  We split below, so this is initialized to one
    // un-split line.
    var listItems = [li];
  
    function walk(node) {
      switch (node.nodeType) {
        case 1:  // Element
          if (nocode.test(node.className)) { break; }
          if ('br' === node.nodeName) {
            breakAfter(node);
            // Discard the <BR> since it is now flush against a </LI>.
            if (node.parentNode) {
              node.parentNode.removeChild(node);
            }
          } else {
            for (var child = node.firstChild; child; child = child.nextSibling) {
              walk(child);
            }
          }
          break;
        case 3: case 4:  // Text
          if (isPreformatted) {
            var text = node.nodeValue;
            var match = text.match(lineBreak);
            if (match) {
              var firstLine = text.substring(0, match.index);
              node.nodeValue = firstLine;
              var tail = text.substring(match.index + match[0].length);
              if (tail) {
                var parent = node.parentNode;
                parent.insertBefore(
                    document.createTextNode(tail), node.nextSibling);
              }
              breakAfter(node);
              if (!firstLine) {
                // Don't leave blank text nodes in the DOM.
                node.parentNode.removeChild(node);
              }
            }
          }
          break;
      }
    }
  
    // Split a line after the given node.
    function breakAfter(lineEndNode) {
      // If there's nothing to the right, then we can skip ending the line
      // here, and move root-wards since splitting just before an end-tag
      // would require us to create a bunch of empty copies.
      while (!lineEndNode.nextSibling) {
        lineEndNode = lineEndNode.parentNode;
        if (!lineEndNode) { return; }
      }
  
      function breakLeftOf(limit, copy) {
        // Clone shallowly if this node needs to be on both sides of the break.
        var rightSide = copy ? limit.cloneNode(false) : limit;
        var parent = limit.parentNode;
        if (parent) {
          // We clone the parent chain.
          // This helps us resurrect important styling elements that cross lines.
          // E.g. in <i>Foo<br>Bar</i>
          // should be rewritten to <li><i>Foo</i></li><li><i>Bar</i></li>.
          var parentClone = breakLeftOf(parent, 1);
          // Move the clone and everything to the right of the original
          // onto the cloned parent.
          var next = limit.nextSibling;
          parentClone.appendChild(rightSide);
          for (var sibling = next; sibling; sibling = next) {
            next = sibling.nextSibling;
            parentClone.appendChild(sibling);
          }
        }
        return rightSide;
      }
  
      var copiedListItem = breakLeftOf(lineEndNode.nextSibling, 0);
  
      // Walk the parent chain until we reach an unattached LI.
      for (var parent;
           // Check nodeType since IE invents document fragments.
           (parent = copiedListItem.parentNode) && parent.nodeType === 1;) {
        copiedListItem = parent;
      }
      // Put it on the list of lines for later processing.
      listItems.push(copiedListItem);
    }
  
    // Split lines while there are lines left to split.
    for (var i = 0;  // Number of lines that have been split so far.
         i < listItems.length;  // length updated by breakAfter calls.
         ++i) {
      walk(listItems[i]);
    }
  
    for (var i = 0, n = listItems.length; i < n; ++i) {
      li = listItems[i];
      // Stick a class on the LIs so that stylesheets can
      // color odd/even rows, or any other row pattern that
      // is co-prime with 10.
      li.className = 'Line' + i + ' line';
      if (!li.firstChild) {
        li.appendChild(document.createTextNode('\xA0'));
      }
      node.appendChild(li);
    }
    return listItems.length;
  }

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
    
    setCloneUrls();


    $(".replaceDialog").each(function(i, form){
        var el = $(form);
        $('button', el).click(function(){
            el.dialog("close");
            return true;
        });
        var newLink = ""
        if(el.attr('data-button-text') !== undefined)
            newLink = "<button class='button'>" + el.attr('data-button-text') + "</button>"
        else 
            newLink = "<a href='javascript://'>" + el.attr('data-anchor-text') + "</a>"
        var newButton = $(newLink).click(function(){
            el.dialog("open");
            return false;
        });
        el.parent().prepend(newButton)
        el.dialog({ autoOpen: false, modal: true, width: "600px" });        
    });

    $(".selectmenu").selectmenu();
    

    $(".blob").each(function(i, el) {
        var code = $("pre code", el);
        hljs.highlightBlock(code[0]);
        var linesCount = numberLines(code[0], true);

        var rawLN = "";
        for(var i = 0; i < linesCount; i++){
            rawLN += "<span class='line_number'>"+ i +"</span>\n";
        }
        
        $(el).append("<div class='source_container'><div class='line_numbers'><pre >"+rawLN+"</pre></div><div class='source'></div></div");
        $(".source", el).append($("pre", el)[0]);
        $(".source_container", el).css("padding-left", $(".line_numbers", el).width());
    });

    CodeScrolling.initFloatingScrollbars($(window));
    DiffHeader.init();
    

    $(".czn").chosen();    
});












