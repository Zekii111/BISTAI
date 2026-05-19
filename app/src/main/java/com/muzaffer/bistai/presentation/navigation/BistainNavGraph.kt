python3 -c "
content = open('app/src/main/java/com/muzaffer/bistai/presentation/navigation/BistaiNavGraph.kt').read()
old = '''    val startDestination = Screen.Portfolio.route // if (authState.isLoading) {
        Screen.Login.route // Yüklenirken de login'de beklet, state değişince LaunchedEffect halleder
    } else if (authState.user != null) {
        Screen.Portfolio.route
    } else {
        Screen.Login.route
    }'''
new = '    val startDestination = Screen.Portfolio.route'
print(content.replace(old, new))
" > /tmp/fixed.kt && mv /tmp/fixed.kt app/src/main/java/com/muzaffer/bistai/presentation/navigation/BistaiNavGraph.kt

git add .
git commit -m "Skip login, go directly to Portfolio"
git push

