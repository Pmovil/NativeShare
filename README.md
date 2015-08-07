Usage:

                    try {
                        if (Share.getInstance().isServiceSupported(Share.TWITTER)) {
                            Share.getInstance().show("Testing twitter native share", "http://www.codenameone.com/img/blog/CodenameOne-Horizontal.png", "image/png", Share.TWITTER);
                        } else {
                            Dialog.show("Oops", "Twitter is not installed", "ok", null);
                        }
                    } catch (RuntimeException ex) {
                        Dialog.show("Oops", ex.getMessage(), "ok", null);
                    }


