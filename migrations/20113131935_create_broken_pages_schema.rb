class CreateBrokenPagesSchema < ActiveRecord::Migration
  def self.up
		create_table :broken_pages do |t|
      t.integer :id, :limit => 11
      t.string :url, :limit => 4000
      t.string :uid, :limit => 32
      t.datetime :timestamp
		end
	end

	def self.down
    drop_table :broken_pages
  end

end
