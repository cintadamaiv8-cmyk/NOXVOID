with open('app/src/main/java/com/example/ui/screens/GroupProfileScreen.kt', 'r') as f:
    code = f.read()

code = code.replace("fun GroupProfileScreen(", "@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun GroupProfileScreen(")
code = code.replace("fun FeatureItem(", "@Composable\nfun FeatureItem(")
code = code.replace("fun OnlineMembersBottomSheet(", "@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun OnlineMembersBottomSheet(")

with open('app/src/main/java/com/example/ui/screens/GroupProfileScreen.kt', 'w') as f:
    f.write(code)

print("Fixed again")
