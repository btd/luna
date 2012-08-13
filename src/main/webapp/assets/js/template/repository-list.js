<div class="new-repo-form">
  <button class="btn btn-large" data-toggle="modal" href="#new-repo-modal" >Add new repository</button>
  <div class="modal hide" id="new-repo-modal">
    <div class="modal-header">
      <button type="button" class="close" data-dismiss="modal">×</button>
      <h3>Choose a name</h3>
    </div>
    <div class="modal-body">
      <input type="text" id="new-repository-name" placeholder="Repository name">
      <span class="help-block">Repository name can have only such symbols: a ... z, A ... Z, 0 ... 9, ., -, !, ~, *, \, (, )</span>
      <label class="checkbox">
        <input type="checkbox" id="new-repository-public"> Make repository public
      </label>
    </div>
    <div class="modal-footer">
      <a href="#" class="btn" data-dismiss="modal">Close</a>
      <a href="#" class="btn btn-primary create-repository">Create</a>
    </div>
  </div>
</div>
<div class="repository-list">
</div>