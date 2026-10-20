"""Export tracked public source only, excluding signing keys and build output."""
from pathlib import Path
import subprocess,zipfile
root=Path(__file__).resolve().parents[1]
subprocess.run(['python3','scripts/check-public.py'],cwd=root,check=True)
names=subprocess.check_output(['git','ls-files','--cached','--others','--exclude-standard','-z'],cwd=root).decode().split('\0')
out=root/'dist/CleanFrame-Android-v1.1.2-source.zip';out.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as archive:
 for name in sorted(set(names)-{''}):
  p=root/name
  if p.is_file(): archive.write(p,'CleanFrame-Android/'+name)
print(out)
