import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery
import groovy.json.JsonOutput

class DefaultUserCredentials {
    String userName;
    String password;
    boolean sampleUser;
    DefaultUserCredentials(userName,password, sampleUser) {
        this.userName=userName;
        this.password=password;
        this.sampleUser=sampleUser;
    }
}

userService = spring.getBean("userService");
authService = spring.getBean("defaultAuthenticationService");

def query = new FlexibleSearchQuery('SELECT {passwordEncoding}, COUNT(0) FROM {User} GROUP BY {passwordEncoding}')
query.setResultClassList([String.class, Long.class])

def result = flexibleSearchService.search(query)

results = []

result.result.each { r -> 
  results << [ encoding: r[0], count: r[1] ]
}

ArrayList userNameList=new ArrayList<DefaultUserCredentials>();
userNameList.add(new DefaultUserCredentials("admin",                            "nimda",      false));
userNameList.add(new DefaultUserCredentials("anonymous",                        "suomynona",  false));
userNameList.add(new DefaultUserCredentials("searchmanager",                    "12341234",   true));
userNameList.add(new DefaultUserCredentials("cmsmanager",                       "1234",       true));
userNameList.add(new DefaultUserCredentials("hac_viewer",                       "viewer",     false));
userNameList.add(new DefaultUserCredentials("hac_editor",                       "editor",     false));
userNameList.add(new DefaultUserCredentials("vjdbcReportsUser",                 "1234",       true));
userNameList.add(new DefaultUserCredentials("hcs_admin",                        "1234",       true));
userNameList.add(new DefaultUserCredentials("productmanager",                   "1234",       true));
userNameList.add(new DefaultUserCredentials("csagent",                          "1234",       true));
userNameList.add(new DefaultUserCredentials("marketingmanager",                 "1234",       true));
userNameList.add(new DefaultUserCredentials("cmsmanager-powertools",            "1234",       true));
userNameList.add(new DefaultUserCredentials("BackofficeProductAdministrator",   "1234",       true));
userNameList.add(new DefaultUserCredentials("BackofficeProductManager",         "1234",       true));
userNameList.add(new DefaultUserCredentials("BackofficeWorkflowAdministrator",  "1234",       true));
userNameList.add(new DefaultUserCredentials("BackofficeWorkflowUser",           "1234",       true));
userNameList.add(new DefaultUserCredentials("analyticsmanager",                 "1234",       true));
userNameList.add(new DefaultUserCredentials("cmseditor",                        "1234",       true));
userNameList.add(new DefaultUserCredentials("cmsreviewer",                      "1234",       true));
userNameList.add(new DefaultUserCredentials("cmstranslator",                    "1234",       true));
userNameList.add(new DefaultUserCredentials("cmstranslator-Annette",            "1234",       true));
userNameList.add(new DefaultUserCredentials("cmstranslator-Seb",                "1234",       true));
userNameList.add(new DefaultUserCredentials("cmspublisher",                     "1234",       true));
 
println "Paste below into confluence using \"+\" -> Markup. A table will then be created. "
println ""
println "|| Status || User || Password Encoding || Comment ||"
 
for (DefaultUserCredentials creds : userNameList)
{
 
    try
    {
        UserModel user=authService.checkCredentials(creds.userName,creds.password);
        println "|(-)|" + creds.userName + "|" + (user.getPasswordEncoding() == null || user.getPasswordEncoding().equals("*") ? "plain text" : user.getPasswordEncoding()) + "|" + (creds.sampleUser ? "Sample user still exists and h" : "H") + "as default password, please change!|"
    }
    catch (de.hybris.platform.servicelayer.security.auth.InvalidCredentialsException e) {
        try {
            UserModel user = userService.getUserForUID(creds.userName);
            if(creds.sampleUser) {
                println "|(!)|" + creds.userName + "|" + (user.getPasswordEncoding() == null || user.getPasswordEncoding().equals("*") ? "plain text" : user.getPasswordEncoding()) + "|Sample user still exists but at least password is not default. |";
            } else {
                println "|(/)|" + creds.userName + "|" + (user.getPasswordEncoding() == null || user.getPasswordEncoding().equals("*") ? "plain text" : user.getPasswordEncoding()) + "|Password changed! All fine. |"
            }
        } catch (UnknownIdentifierException) {
            println "|(/)|" + creds.userName + "|-|Does not exists. All fine. |"
        }
 
 
    }
 
}

JsonOutput.prettyPrint(JsonOutput.toJson(results))