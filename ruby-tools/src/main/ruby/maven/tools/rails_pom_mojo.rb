require File.join(File.dirname(__FILE__), 'rails_pom.rb')

warn "deprecated, use rails_project instead"

# TODO usage
maven = Maven::Tools::RailsPom.new(eval(ARGV[0]))
pom = maven.create_pom(File.new(ARGV[1]))
puts pom.to_xml
