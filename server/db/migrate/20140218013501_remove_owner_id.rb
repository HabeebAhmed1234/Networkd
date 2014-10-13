class RemoveOwnerId < ActiveRecord::Migration
  def change
  	remove_column :conversations, :owner_id
  end
end
