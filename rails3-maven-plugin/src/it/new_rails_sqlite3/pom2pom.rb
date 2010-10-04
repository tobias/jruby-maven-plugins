require 'fileutils'
Dir.glob("rails_app/*").each do |f| 
  FileUtils.mv(f, f.sub(/rails_app./, ''))
end
FileUtils.rm_r("rails_app")
pom = File.read('pom.xml').sub(/>war</,'>pom<')
File.open('pom-base.xml', 'w') { |f| f.print(pom) }
File.rename('pom-server.xml', 'pom.xml')
