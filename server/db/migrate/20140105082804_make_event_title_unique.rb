class MakeEventTitleUnique < ActiveRecord::Migration
  def change
  	add_index :events, :title, :unique => true
  end
end
