# anonymous support class for blacklisting whole recipes
# based on angstrom.bbclass
# 
# Features:
#
# * blacklist handling, set BLACKLIST_pn-blah = "message"
#

python () {
    import bb

    blacklist = bb.data.getVar("BLACKLIST", d, 1)
    pkgnm = bb.data.getVar("PN", d, 1)

    if blacklist:
	bb.note("%s is blacklisted because %s" % (pkgnm, blacklist))
        raise bb.parse.SkipPackage("%s is blacklisted because %s" % (pkgnm, blacklist))

}

