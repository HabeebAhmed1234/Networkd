class RemoveEventRelations < ActiveRecord::Migration
  def change
  	remove_index :users, :event_id
  end
end
