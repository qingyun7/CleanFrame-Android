#!/usr/bin/env python3
"""Build with JDK 17, Android SDK 34 and Gradle 8.9. Signing keys stay local."""
from pathlib import Path
import os, shutil, subprocess, secrets
root=Path(__file__).resolve().parent
env=dict(os.environ)
gradle=env.get('CLEANFRAME_GRADLE') or shutil.which('gradle')
if not gradle: raise SystemExit('Install Gradle 8.9 or set CLEANFRAME_GRADLE to its executable.')
java=Path(env['JAVA_HOME'])/'bin/keytool' if env.get('JAVA_HOME') else shutil.which('keytool')
if not java: raise SystemExit('Install JDK 17 and set JAVA_HOME.')
key=root/'.local/cleanframe-local.jks';key.parent.mkdir(exist_ok=True)
props=root/'.local/signing.properties'
if key.exists() != props.exists():
 raise SystemExit('Keep the signing keystore and .local/signing.properties together. Restore the missing file before building.')
if not key.exists():
 password=secrets.token_urlsafe(32)
 env['CLEANFRAME_KEYSTORE_PASSWORD']=password
 subprocess.run([str(java),'-genkeypair','-noprompt','-keystore',str(key),'-storepass:env','CLEANFRAME_KEYSTORE_PASSWORD','-alias','cleanframe','-keypass:env','CLEANFRAME_KEYSTORE_PASSWORD','-dname','CN=CleanFrame Local Android','-keyalg','RSA','-keysize','2048','-validity','10000'],check=True,env=env)
 props.write_text('storePassword='+password+'\nkeyPassword='+password+'\nkeyAlias=cleanframe\n')
 props.chmod(0o600)
 env.pop('CLEANFRAME_KEYSTORE_PASSWORD',None)
command=[str(gradle),'--no-daemon','--max-workers=2','--console=plain']
if env.get('CLEANFRAME_OFFLINE')=='1': command.append('--offline')
subprocess.run(command+['assembleRelease'],cwd=root,env=env,check=True)
print(root/'app/build/outputs/apk/release/app-release.apk')
