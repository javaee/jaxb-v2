cd $JAXB_HOME/dist/docs
cvs-socks -d:pserver:ryan_shoemaker@cvs.dev.java.net:/cvs -z3 import -ko -W "*.png -k 'b'" -W "*.gif -k 'b'" -W "*.zip -k 'b'" -m "deploying the new release notes" jaxb/www/jaxb20-ea/docs site-deployment t`date +%Y%m%d-%H%M%S`

cd ../../../jaxb/www
date >> update.html
cvs-socks commit -m "to work around a bug in java.net web updater" update.html

