===============
Rule Versioning
===============

It is possible to use git integration and rollback coin rules to previous versions.
This will also roll back all affected CDRs and bills

Setup rule versioning
---------------------

To enable this functionality, a git repository needs to be created for the rules.
The credentials for this repository need to be specified in the *conf* file of
the *coin* service.

::

  # Git credentials:
  GitRepo=
  GitUsername=
  GitPassword=
  GitProjectPath=

Setting up checkpoints for the rules
------------------------------------

To be able to roll back rules, they need to be uploaded to the git repository
after they are applied to *coin*. Different versions of the rules can be **tagged**
for easier reference.

Rolling back rules and affected records
---------------------------------------

To rollback rules to a previous version, a command request needs to be made to
*coin* with the following format:

::

  {
    "commits": [
        {
            "added": [
                <list of rules added by the commit>
            ],
            "modified": [
                <list of rules modified by the commit>
            ]
        }
    ],
    "project_id": <id of the project>,
    "ref": <tag or branch to roll back to>,
    "time_from": <time of the commit to be undone>
  }

A list of 'bad' commits can be provided, with lists of files added or modified
in those commits. The **time_from** parameter is important, as it will set the
checkpoint for the rolling back of CDRs and bills.
This request is made to:

::

  <coinurl>/newrule?execute=true

The execute parameter forces all rules to be fired when a rule is rolled back.
