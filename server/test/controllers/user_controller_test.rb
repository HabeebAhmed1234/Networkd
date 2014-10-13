require 'test_helper'

class UserControllerTest < ActionController::TestCase
  test "should get show" do
    get :show
    assert_response :success
  end

  test "should get create" do
    get :create
    assert_response :success
  end

  test "should get update_gps" do
    get :update_gps
    assert_response :success
  end

  test "should get attend_event" do
    get :attend_event
    assert_response :success
  end

  test "should get add_event" do
    get :add_event
    assert_response :success
  end

  test "should get update" do
    get :update
    assert_response :success
  end

end
