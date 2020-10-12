# MailmanMod

* an Android app for moderating Mailman v2 mailing lists
  * parses the HTML output for lack of a JSON API, incompatible with Mailman v3 – unlikely to change anytime soon unless my university switches to v3
* periodically polls Mailman for pending moderation requests
* as of now, the app can only be used for accepting or discarding held mail, not for subscription requests
* to set up a mailing list, specify the moderation URL (typically `https://[server]/mailman/admindb/[listname]`), password and update interval

